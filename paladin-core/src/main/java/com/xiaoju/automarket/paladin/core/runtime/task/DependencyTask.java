package com.xiaoju.automarket.paladin.core.runtime.task;

import akka.actor.AbstractActorWithStash;
import akka.actor.Props;
import com.google.common.base.Preconditions;
import com.xiaoju.automarket.paladin.core.common.ExecutionStateEnum;
import com.xiaoju.automarket.paladin.core.runtime.dcg.ConditionDescriptor;
import com.xiaoju.automarket.paladin.core.runtime.handler.ConditionHandler;
import com.xiaoju.automarket.paladin.core.runtime.message.*;
import com.xiaoju.automarket.paladin.core.util.ReflectionUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @Author Luogh
 * @Date 2020/12/15
 **/
@Slf4j
public class DependencyTask extends AbstractActorWithStash {

    private final AtomicReference<ExecutionStateEnum> taskStatus;
    private final String dependencyId;
    private final ConditionHandler conditionHandler;
    private final TaskContext taskContext;
    private final Map<String, RegisterAction> downstreamActions;


    private DependencyTask(String dependencyId, ConditionHandler conditionHandler,
                           TaskContext taskContext,
                           Map<String, RegisterAction> downstreamActions) {
        this.dependencyId = dependencyId;
        this.conditionHandler = conditionHandler;
        this.taskContext = taskContext;
        this.downstreamActions = downstreamActions;

        AtomicReference<ExecutionStateEnum> taskStatus = new AtomicReference<>();
        taskStatus.set(ExecutionStateEnum.DEPLOYED);
        this.taskStatus = taskStatus;
    }


    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(TaskInitStateRequest.class, msg -> {
                    conditionHandler.initialize(taskContext.getConfig());
                    getContext().become(onInitializeState());
                    getSender().tell(AcknowledgeResponse.getInstance(), self());
                    taskStatus.set(ExecutionStateEnum.INITIALIZED);

                }).match(TaskRunningStateRequest.class, msg -> {
                    unstashAll();
                    getContext().become(onRunningState());
                    getSender().tell(AcknowledgeResponse.getInstance(), self());
                    taskStatus.set(ExecutionStateEnum.RUNNING);
                }).match(TaskCancelStateRequest.class, msg -> {
                    // TODO
                })
                .build();
    }

    private Receive onInitializeState() {
        return receiveBuilder()
                .match(TaskDependencyCheckRequest.class, msg -> {
                    log.warn("receive event message,but task is not in running status, current status is : {}", taskStatus.get());
                    stash();
                })
                .match(TaskDependencyRouterRequest.class, msg -> {
                    log.warn("receive event message,but task is not in running status, current status is : {}", taskStatus.get());
                    stash();
                })
                .build();
    }

    private Receive onRunningState() {
        return receiveBuilder()
                .match(TaskDependencyCheckRequest.class, msg -> {
                    TaskDependencyResponse response = new TaskDependencyResponse();
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
                .match(TaskDependencyRouterRequest.class, msg -> {
                    RegisterAction registerAction = this.downstreamActions.get(msg.getActionId());
                    Preconditions.checkArgument(registerAction != null, "downstream action " + msg.getActionId() + " not found, but received a router request.");
                    registerAction.getTaskRef().forward(msg.getEvent(), context());
                }).build();
    }


    public static Props props(String dependencyId, ConditionDescriptor conditionDescriptor, TaskContext taskContext, Map<String, RegisterAction> downstreamActions) {
        ConditionHandler conditionHandler = ReflectionUtil.newInstance(conditionDescriptor.getConditionHandler());
        return Props.create(DependencyTask.class, () -> new DependencyTask(dependencyId, conditionHandler, taskContext, downstreamActions));
    }
}
