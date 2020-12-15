package com.xiaoju.automarket.paladin.core.runtime.task;

import akka.actor.AbstractActorWithStash;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.japi.pf.ReceiveBuilder;
import akka.pattern.Patterns;
import akka.util.Timeout;
import com.google.common.base.Preconditions;
import com.xiaoju.automarket.paladin.core.common.StatusEnum;
import com.xiaoju.automarket.paladin.core.dcg.DependencyDescriptor.DependencyDescriptorView;
import com.xiaoju.automarket.paladin.core.runtime.handler.ActionHandler;
import com.xiaoju.automarket.paladin.core.runtime.handler.DependencySelectorStrategy;
import com.xiaoju.automarket.paladin.core.runtime.task.Messages.*;
import com.xiaoju.automarket.paladin.core.util.FutureUtil;
import lombok.extern.slf4j.Slf4j;
import scala.concurrent.Future;
import scala.reflect.ClassTag;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static java.util.stream.Collectors.toList;

/**
 * @Author Luogh
 * @Date 2020/12/15
 **/
@Slf4j
public class ActionTask extends AbstractActorWithStash {

    private final AtomicReference<StatusEnum> taskStatus;
    private final int actionId;
    private final ActionHandler actionHandler;
    private final TaskContext taskContext;
    private final Map<Integer, RegisterDependency> upperStreamDependencies;
    private final Map<Integer, RegisterDependency> downStreamDependencies;
    private final DependencySelectorStrategy dependencySelectorStrategy;


    private ActionTask(int actionId, ActionHandler actionHandler, TaskContext taskContext,
                       DependencySelectorStrategy dependencySelectorStrategy) {
        this(actionId, actionHandler, taskContext, dependencySelectorStrategy, new ConcurrentHashMap<>(), new ConcurrentHashMap<>());
    }

    private ActionTask(int actionId, ActionHandler actionHandler, TaskContext taskContext,
                       DependencySelectorStrategy dependencySelectorStrategy,
                       Map<Integer, RegisterDependency> upperStreamDependencies,
                       Map<Integer, RegisterDependency> downstreamDependencies) {
        this.actionId = actionId;
        this.actionHandler = actionHandler;
        this.taskContext = taskContext;
        this.dependencySelectorStrategy = dependencySelectorStrategy;
        this.downStreamDependencies = downstreamDependencies;
        this.upperStreamDependencies = upperStreamDependencies;

        AtomicReference<StatusEnum> taskStatus = new AtomicReference<>();
        taskStatus.set(StatusEnum.DEPLOYED);
        this.taskStatus = taskStatus;
    }

    @Override
    public void preStart() throws Exception {
        final List<? extends Class<? extends SubscriptionEvent>> subscribeEventTypes = this.actionHandler.subscribeEventTypes();
        if (subscribeEventTypes != null && !subscribeEventTypes.isEmpty()) {
            for (Class<? extends SubscriptionEvent> eventType : subscribeEventTypes) {
                boolean success = context().system().eventStream().subscribe(getSelf(), eventType);
                log.info("action task subscribe event type: {} with result: {}", eventType, success);
            }
        }
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(TaskInitStateRequest.class,
                        request -> {
                            try {
                                actionHandler.initialize(taskContext.getConfig());
                                getContext().become(onInitializeState());
                                Response<Boolean> response = new Response<>();
                                response.setSuccess(true);
                                getSender().tell(response, self());
                                taskStatus.set(StatusEnum.INITIALIZED);
                            } catch (Throwable e) {
                                log.error("task init failed for action:" + actionId, e);
                                Response<Boolean> response = new Response<>();
                                response.setSuccess(false);
                                response.setException(e);
                                getSender().tell(response, self());
                                taskStatus.set(StatusEnum.FAILED);
                            }
                        })
                .match(TaskRunningStateRequest.class, msg -> {
                    try {
                        unstashAll();
                        getContext().become(onRunningState());
                        taskStatus.set(StatusEnum.RUNNING);
                    } catch (Exception e) {
                        log.error("task switch state: " + taskStatus.get() + " to running state failed for action:" + actionId, e);
                        Response<Boolean> response = new Response<>();
                        response.setSuccess(false);
                        response.setException(e);
                        getSender().tell(response, self());
                        taskStatus.set(StatusEnum.FAILED);
                    }
                }).match(TaskCancelStateRequest.class, msg -> {
                    // TODO
                })
                .build();
    }

    private ReceiveBuilder dependencyMsgReceiveBuilder() {
        return receiveBuilder()
                .match(RegisterDependency.class, msg -> {
                    if (msg.isOutside()) {
                        registerDependency(downStreamDependencies, msg);
                    } else {
                        registerDependency(upperStreamDependencies, msg);
                    }
                })
                .match(UnRegisterDependencyRequest.class, msg -> {
                    if (msg.isOutside()) {
                        downStreamDependencies.remove(msg.getDependencyId());
                    } else {
                        upperStreamDependencies.remove(msg.getDependencyId());
                    }
                });
    }

    private void registerDependency(Map<Integer, RegisterDependency> dependencyMap, RegisterDependency dependency) {
        Preconditions.checkArgument(dependencyMap != null);
        if (!dependencyMap.containsKey(dependency.getDependencyId())) {
            dependencyMap.put(dependency.getDependencyId(), dependency);
            log.warn("registered dependency: {} for action: {} with total {} downstream dependencies yet.", dependency.getDependencyId(), this.actionId, dependencyMap.size());
        } else {
            log.warn("already register dependency: {} for action: {}, ignore it.", dependency, this.actionId);
        }
    }

    private Receive onInitializeState() {
        return dependencyMsgReceiveBuilder()
                .match(SubscriptionEvent.class, msg -> {
                    log.warn("receive event message,but task is not in running status, current status is : {}", taskStatus.get());
                    stash();
                })
                .build();
    }

    private Receive onRunningState() {
        Timeout timeout = new Timeout(1, TimeUnit.SECONDS);
        final ActorRef self = self();
        return dependencyMsgReceiveBuilder()
                .match(SubscriptionEvent.class, msg -> {
                    ActionHandler.ActionResult actionResult = this.actionHandler.doAction(msg);
                    if (!this.downStreamDependencies.isEmpty()) {
                        Preconditions.checkArgument(actionResult != null && actionResult.getEvent() != null, "null result");
                        if (actionResult.getFireDuration() == null || actionResult.getFireDuration() == Duration.ZERO) {
                            processEvent(self, actionResult, timeout);
                        } else {
                            context().system().getScheduler().scheduleOnce(actionResult.getFireDuration(), () -> {
                                processEvent(self, actionResult, timeout);
                            }, context().dispatcher());
                        }
                    }
                }).build();
    }

    private void processEvent(ActorRef self, ActionHandler.ActionResult actionResult, Timeout timeout) {
        final TaskDependencyCheckRequest request = new TaskDependencyCheckRequest();
        request.setEvent(actionResult.getEvent());
        final List<CompletableFuture<TaskDependencyResponse>> dependencyResults = downStreamDependencies.values().parallelStream()
                .map(dependency -> {
                    Future<TaskDependencyResponse> future = Patterns.ask(dependency.getTaskRef(), request, timeout)
                            .mapTo(ClassTag.apply(TaskDependencyResponse.class));
                    return FutureUtil.toJava(future);
                }).collect(toList());

        FutureUtil.sequence(dependencyResults).whenCompleteAsync((responses, throwable) -> {
            final List<DependencyDescriptorView> candidates = responses.stream().filter(TaskDependencyResponse::isMatched).map(response -> {
                if (response.getException() != null) {
                    if (response.getException() instanceof RuntimeException) {
                        throw (RuntimeException) response.getException();
                    } else {
                        throw new RuntimeException(response.getException());
                    }
                } else {
                    return downStreamDependencies.get(response.getDependencyId()).getDependency();
                }
            }).collect(toList());

            List<DependencyDescriptorView> chooseDependencies = dependencySelectorStrategy.select(actionResult.getEvent(), candidates);
            Preconditions.checkArgument(chooseDependencies != null && !chooseDependencies.isEmpty(), "not found any dependencies for current event in action:" + actionId);

            for (DependencyDescriptorView dependency : chooseDependencies) {
                RegisterDependency registerDependency = downStreamDependencies.get(dependency.getDependencyId());
                // send event to next action
                TaskDependencyRouterRequest routerRequest = new TaskDependencyRouterRequest();
                routerRequest.setActionId(dependency.getNextActionId());
                routerRequest.setEvent(actionResult.getEvent());
                registerDependency.getTaskRef().tell(actionResult.getEvent(), self);
            }
        });
    }


    public static Props props(int actionId, ActionHandler actionHandler, TaskContext taskContext, DependencySelectorStrategy dependencySelectorStrategy) {
        return Props.create(ActionTask.class, () -> new ActionTask(actionId, actionHandler, taskContext, dependencySelectorStrategy));
    }
}
