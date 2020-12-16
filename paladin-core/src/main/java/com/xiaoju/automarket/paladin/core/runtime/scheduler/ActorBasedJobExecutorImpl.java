package com.xiaoju.automarket.paladin.core.runtime.scheduler;


import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.pattern.Patterns;
import com.google.common.base.Preconditions;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Table;
import com.xiaoju.automarket.paladin.core.common.ExecutionStateEnum;
import com.xiaoju.automarket.paladin.core.runtime.dcg.ActionDescriptor;
import com.xiaoju.automarket.paladin.core.runtime.dcg.DependencyDescriptor;
import com.xiaoju.automarket.paladin.core.runtime.dcg.JobGraphDescriptor;
import com.xiaoju.automarket.paladin.core.runtime.job.JobEnvironment;
import com.xiaoju.automarket.paladin.core.runtime.job.JobExecutor;
import com.xiaoju.automarket.paladin.core.runtime.job.JobInstance;
import com.xiaoju.automarket.paladin.core.runtime.message.*;
import com.xiaoju.automarket.paladin.core.runtime.task.ActionTask;
import com.xiaoju.automarket.paladin.core.runtime.task.DependencyTask;
import com.xiaoju.automarket.paladin.core.runtime.task.TaskContext;
import com.xiaoju.automarket.paladin.core.runtime.util.AkkaUtil;
import com.xiaoju.automarket.paladin.core.util.FutureUtil;
import com.xiaoju.automarket.paladin.core.util.Util;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * @Author Luogh
 * @Date 2020/12/6
 **/
@Slf4j
public class ActorBasedJobExecutorImpl implements JobExecutor {

    private final String jobExecutorId = String.format("JobExecutor-%s", Util.generateUUID());

    private JobEnvironment environment;

    private ActorSystem actorSystem;

    /* Table RowKey: jobId, ColumnName: taskId(action/dependency), ColumnValue: taskLocation*/
    private final Table<JobInstance, String, ActorRef> jobTables = HashBasedTable.create();

    private final Object JOB_TABLE_LOCK = new Object();

    @Override
    public void configure(JobEnvironment environment) {
        this.environment = environment;
        this.actorSystem = AkkaUtil.createActorSystem(environment.configuration());
    }

    @Override
    public String getJobExecutorId() {
        return this.jobExecutorId;
    }

    @Override
    public void deployJob(JobInstance jobInstance) {
        synchronized (JOB_TABLE_LOCK) {
            Preconditions.checkArgument(!jobTables.containsRow(jobInstance),
                    String.format("job executor: [%s] already deployed job: [%s], but current received a new job instance: [%s]",
                            this.jobExecutorId, jobInstance.getJobId(), jobInstance)
            );

            JobGraphDescriptor graphDescriptor = jobInstance.getGraphDescriptor();
            Map<String, ActionDescriptor> actions = graphDescriptor.getActions();
            actions.entrySet().forEach(entry -> deployAction(jobInstance, entry));

            Map<String, DependencyDescriptor> dependencies = graphDescriptor.getDependencies();
            dependencies.entrySet().forEach(dependEntry -> deployDependency(jobInstance, dependEntry));

            this.environment.jobStore().updateJobStatus(jobInstance.getJobId(), ExecutionStateEnum.DEPLOYED, null);

            // after all tasks deployed completed, do task initialize operation.
            Map<String, ActorRef> tasks = jobTables.row(jobInstance);
            Preconditions.checkArgument(tasks != null && !tasks.isEmpty());

            List<CompletableFuture<AcknowledgeResponse>> futures = tasks.values().stream()
                    .map(actorRef ->
                            Patterns.ask(actorRef, TaskInitStateRequest.getInstance(), FutureUtil.INF_DURATION)
                                    .toCompletableFuture().thenApply(AcknowledgeResponse.class::cast))
                    .collect(Collectors.toList());

            FutureUtil.sequence(futures).thenComposeAsync(responses -> {
                List<CompletableFuture<AcknowledgeResponse>> futureLists = registerAllDependencies(actions, tasks);
                return FutureUtil.sequence(futureLists);
            }).whenCompleteAsync((success, throwable) -> {
                if (throwable != null) {
                    // exception, stop all tasks
                    log.error("deploy job : " + jobInstance.getJobId() + " failed.", throwable);

                    tasks.entrySet().parallelStream().forEach(actionEntry -> {
                        // TODO using ask instead
                        actionEntry.getValue().tell(TaskCancelStateRequest.getInstance(), ActorRef.noSender());
                        this.jobTables.remove(jobInstance, actionEntry.getKey());
                    });

                    this.environment.jobStore().updateJobStatus(jobInstance.getJobId(), ExecutionStateEnum.CANCELLED, null);
                } else {
                    log.error("deploy job : " + jobInstance.getJobId() + " success, swith to running state.");
                    // TODO using ask instead
                    tasks.entrySet().parallelStream().forEach(actionEntry -> {
                        actionEntry.getValue().tell(TaskRunningStateRequest.getInstance(), ActorRef.noSender());
                    });
                    this.environment.jobStore().updateJobStatus(jobInstance.getJobId(), ExecutionStateEnum.RUNNING, null);
                }
            });
        }
    }

    @Override
    public void cancelJob(String jobId) {

    }

    private List<CompletableFuture<AcknowledgeResponse>> registerAllDependencies(Map<String, ActionDescriptor> actions, Map<String, ActorRef> tasks) {
        List<CompletableFuture<AcknowledgeResponse>> futureLists = new ArrayList<>();
        // all task init complete success, start register all dependency to action node
        for (Map.Entry<String, ActionDescriptor> entry : actions.entrySet()) {
            ActorRef actionActorRef = tasks.get(entry.getKey());
            if (entry.getValue().getDownstreamDependencies() != null) {
                futureLists.addAll(registerDependency(entry.getValue().getDownstreamDependencies(), true, actionActorRef, tasks));
            }

            if (entry.getValue().getUpstreamDependencies() != null) {
                futureLists.addAll(registerDependency(entry.getValue().getUpstreamDependencies(), false, actionActorRef, tasks));
            }
        }
        return futureLists;
    }

    private void deployDependency(JobInstance jobInstance, Map.Entry<String, DependencyDescriptor> dependEntry) {
        DependencyDescriptor dependencyDescriptor = dependEntry.getValue();
        if (!jobTables.contains(jobInstance, dependencyDescriptor.getDependencyId())) {
            TaskContext context = new TaskContext(this.actorSystem, this.environment.configuration(), dependEntry.getKey(), dependencyDescriptor.getDependencyName());
            ActionDescriptor nextActionDescriptor = dependencyDescriptor.getNextActionDescriptor();
            if (nextActionDescriptor != null) {
                ActorRef nextAction = this.jobTables.get(jobInstance, nextActionDescriptor.getActionId());
                Preconditions.checkArgument(nextAction != null,
                        "dependency: " + dependencyDescriptor.getDependencyId() + " rely on next action: " + nextActionDescriptor.getActionId() + " but not found in initialized actorRef map");

                RegisterAction registerAction = new RegisterAction(nextActionDescriptor.getActionId(), nextActionDescriptor.getActionName(), nextAction);
                Map<String, RegisterAction> downstreamActions = ImmutableMap.of(nextActionDescriptor.getActionId(), registerAction);
                Props props = DependencyTask.props(dependEntry.getKey(), dependencyDescriptor.getConditionDescriptor(), context, downstreamActions);
                ActorRef actorRef = this.actorSystem.actorOf(props, context.getTaskInstanceId());

                this.jobTables.put(jobInstance, dependencyDescriptor.getDependencyId(), actorRef);
            }
        }
    }

    private void deployAction(JobInstance jobInstance, Map.Entry<String, ActionDescriptor> entry) {
        ActionDescriptor actionDescriptor = entry.getValue();
        if (!jobTables.contains(jobInstance, actionDescriptor.getActionId())) {
            TaskContext context = new TaskContext(this.actorSystem, this.environment.configuration(), entry.getKey(), actionDescriptor.getActionName());
            Props props = ActionTask.props(entry.getKey(), actionDescriptor.getActionHandler(), context, actionDescriptor.getDependencySelectorStrategy());
            ActorRef actorRef = this.actorSystem.actorOf(props, context.getTaskInstanceId());
            if (actorRef != null) {
                this.jobTables.put(jobInstance, actionDescriptor.getActionId(), actorRef);
            }
        } else {
            log.warn("already create actorRef for action: {}, ignore current initialize op", actionDescriptor.getActionName());
        }
    }

    private List<CompletableFuture<AcknowledgeResponse>> registerDependency(List<DependencyDescriptor> dependencyDescriptors,
                                                                            boolean isOutSide, ActorRef actionActorRef,
                                                                            Map<String, ActorRef> taskRefs) {
        List<CompletableFuture<AcknowledgeResponse>> futureLists = new ArrayList<>();
        for (DependencyDescriptor dependencyDescriptor : dependencyDescriptors) {
            ActorRef dependencyRef = taskRefs.get(dependencyDescriptor.getDependencyId());
            Preconditions.checkArgument(dependencyRef != null, "not found dependency ref:" + dependencyDescriptor.getDependencyId());
            RegisterDependency registerDependency = new RegisterDependency(isOutSide, dependencyDescriptor.getDependencyId(), dependencyRef, dependencyDescriptor.toView());
            CompletableFuture<AcknowledgeResponse> future = Patterns.ask(actionActorRef, registerDependency, FutureUtil.INF_DURATION)
                    .toCompletableFuture()
                    .thenApply(AcknowledgeResponse.class::cast);
            futureLists.add(future);
        }
        return futureLists;
    }

}
