package com.xiaoju.automarket.paladin.core.runtime.task;

import akka.actor.AbstractActorWithStash;
import akka.actor.Props;
import com.google.common.base.Preconditions;
import com.xiaoju.automarket.paladin.core.common.StatusEnum;
import com.xiaoju.automarket.paladin.core.runtime.handler.ConditionHandler;
import com.xiaoju.automarket.paladin.core.runtime.task.Messages.RegisterAction;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @Author Luogh
 * @Date 2020/12/15
 **/
@Slf4j
public class DependencyTask extends AbstractActorWithStash {

    private final AtomicReference<StatusEnum> taskStatus;
    private final int dependencyId;
    private final ConditionHandler conditionHandler;
    private final TaskContext taskContext;
    private final Map<Integer, RegisterAction> downstreamActions;


    private DependencyTask(int dependencyId, ConditionHandler conditionHandler,
                          TaskContext taskContext,
                          Map<Integer, RegisterAction> downstreamActions) {
        this.dependencyId = dependencyId;
        this.conditionHandler = conditionHandler;
        this.taskContext = taskContext;
        this.downstreamActions = downstreamActions;

        AtomicReference<StatusEnum> taskStatus = new AtomicReference<>();
        taskStatus.set(StatusEnum.DEPLOYED);
        this.taskStatus = taskStatus;
    }


    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(Messages.TaskInitStateRequest.class, msg -> {
                    try {
                        conditionHandler.initialize(taskContext.getConfig());
                        getContext().become(onInitializeState());
                        Messages.Response<Boolean> response = new Messages.Response<>();
                        response.setSuccess(true);
                        getSender().tell(response, self());
                        taskStatus.set(StatusEnum.INITIALIZED);
                    } catch (Throwable e) {
                        log.error("task init failed for dependency node:" + dependencyId, e);
                        Messages.Response<Boolean> response = new Messages.Response<>();
                        response.setSuccess(false);
                        response.setException(e);
                        getSender().tell(response, self());
                        taskStatus.set(StatusEnum.FAILED);
                    }
                }).match(Messages.TaskRunningStateRequest.class, msg -> {
                    try {
                        unstashAll();
                        getContext().become(onRunningState());
                        taskStatus.set(StatusEnum.RUNNING);
                    } catch (Exception e) {
                        log.error("task switch state: " + taskStatus.get() + " to running state failed for dependency:" + dependencyId, e);
                        Messages.Response<Boolean> response = new Messages.Response<>();
                        response.setSuccess(false);
                        response.setException(e);
                        getSender().tell(response, self());
                        taskStatus.set(StatusEnum.FAILED);
                    }
                }).match(Messages.TaskCancelStateRequest.class, msg -> {
                    // TODO
                })
                .build();
    }

    private Receive onInitializeState() {
        return receiveBuilder()
                .match(Messages.TaskDependencyCheckRequest.class, msg -> {
                    log.warn("receive event message,but task is not in running status, current status is : {}", taskStatus.get());
                    stash();
                })
                .match(Messages.TaskDependencyRouterRequest.class, msg -> {
                    log.warn("receive event message,but task is not in running status, current status is : {}", taskStatus.get());
                    stash();
                })
                .build();
    }

    private Receive onRunningState() {
        return receiveBuilder()
                .match(Messages.TaskDependencyCheckRequest.class, msg -> {
                    Messages.TaskDependencyResponse response = new Messages.TaskDependencyResponse();
                    response.setDependencyId(this.dependencyId);
                    try {
                        boolean isMatched = conditionHandler.doCheck(msg.getEvent());
                        response.setMatched(isMatched);
                    } catch (Throwable exception) {
                        log.error("condition handler check failed at dependency: " + dependencyId, exception);
                        response.setException(exception);
                    }
                    sender().tell(response, self());
                })
                .match(Messages.TaskDependencyRouterRequest.class, msg -> {
                    RegisterAction registerAction = this.downstreamActions.get(msg.getActionId());
                    Preconditions.checkArgument(registerAction != null, "downstream action " + msg.getActionId() + " not found, but received a router request.");
                    registerAction.getTaskRef().forward(msg.getEvent(), context());
                }).build();
    }


    public static Props props(int dependencyId, ConditionHandler conditionHandler, TaskContext taskContext, Map<Integer, RegisterAction> downstreamActions) {
        return Props.create(DependencyTask.class, () -> new DependencyTask(dependencyId, conditionHandler, taskContext, downstreamActions));
    }
}
