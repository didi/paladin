package com.xiaoju.automarket.paladin.core.runtime;


import com.google.common.base.Preconditions;
import com.xiaoju.automarket.paladin.core.common.JobStatus;
import com.xiaoju.automarket.paladin.core.util.Util;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @Author Luogh
 * @Date 2020/12/6
 **/
@Slf4j
public class ActorBasedJobExecutorImpl implements JobExecutor {

    private JobEnvironment environment;

    private final AtomicReference<JobInstance> jobInstanceHolder = new AtomicReference<>(null);

    private final String jobExecutorId = String.format("JobExecutor-%s", Util.generateUUID());


    @Override
    public void configure(JobEnvironment environment) {
        this.environment = environment;
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
        this.environment.jobStore().updateJobStatus(newJob.getJobId(), JobStatus.DEPLOYED, null);
        // initialized
        // running
        return null;
    }

    @Override
    public CompletableFuture<Void> cancelJob(String jobId) {
        return null;
    }

}
