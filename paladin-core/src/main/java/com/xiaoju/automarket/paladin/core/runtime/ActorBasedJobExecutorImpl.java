package com.xiaoju.automarket.paladin.core.runtime;


import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import com.google.common.base.Preconditions;
import com.xiaoju.automarket.paladin.core.common.JobStatusEnum;
import com.xiaoju.automarket.paladin.core.dcg.ActionDescriptor;
import com.xiaoju.automarket.paladin.core.dcg.JobGraphDescriptor;
import com.xiaoju.automarket.paladin.core.runtime.util.AkkaUtil;
import com.xiaoju.automarket.paladin.core.util.Util;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @Author Luogh
 * @Date 2020/12/6
 **/
@Slf4j
public class ActorBasedJobExecutorImpl implements JobExecutor {

    private final AtomicReference<JobInstance> jobInstanceHolder = new AtomicReference<>(null);

    private final String jobExecutorId = String.format("JobExecutor-%s", Util.generateUUID());

    private JobEnvironment environment;

    private ActorSystem actorSystem;

    private final Map<Integer, ActorRef> action2ActorRefMap = new HashMap<>();

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
    public CompletableFuture<Void> deployJob(JobInstance newJob) {
        JobInstance existedJob = jobInstanceHolder.get();
        Preconditions.checkArgument(existedJob == null && newJob != null,
                String.format("job executor: [%s] already deployed job: [%s], but current received a new job: [%s]",
                        this.jobExecutorId, existedJob != null ? existedJob.getJobId() : null, newJob.getJobId())
        );
        this.jobInstanceHolder.set(newJob);
        // deployed
        deployActionTasksInJobGraph(newJob.getGraphDescriptor());
        this.environment.jobStore().updateJobStatus(newJob.getJobId(), JobStatusEnum.DEPLOYED, null);
        // initialized
        // running
        return null;
    }

    private void deployActionTasksInJobGraph(JobGraphDescriptor jobGraphDescriptor) {
        Map<Integer, ActionDescriptor> actions = jobGraphDescriptor.getActions();
        for (Map.Entry<Integer, ActionDescriptor> entry : actions.entrySet()) {
            if (!this.action2ActorRefMap.containsKey(entry.getKey())) {
                Class<? extends ActionHandler> clazz = entry.getValue().getActionHandler();
                ActorRef actorRef;
                if (clazz.isAssignableFrom(IngestActionHandler.class)) {
                    actorRef = actorSystem.actorOf(IngestActionTask.apply(this.environment, entry.getValue()));
                } else if (clazz.isAssignableFrom(TransformActionHandler.class)) {
                    actorRef = actorSystem.actorOf(TransformActionTask.apply(this.environment, entry.getValue()));
                } else if (clazz.isAssignableFrom(TransformActionHandler.class)) {
                    actorRef = actorSystem.actorOf(SinkActionTask.apply(this.environment, entry.getValue()));
                } else {
                    throw new RuntimeException("unsupported action handler implement specified." + clazz);
                }

                if (actorRef != null) {
                    this.action2ActorRefMap.put(entry.getKey(), actorRef);
                }
            } else {
                log.warn("already create actorRef for {}, ignore current initialize op", entry.getKey());
            }
        }
    }

    @Override
    public CompletableFuture<Void> cancelJob(String jobId) {
        return null;
    }

}
