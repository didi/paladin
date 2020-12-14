package com.xiaoju.automarket.paladin.core.runtime;

import com.google.common.collect.Sets;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.typesafe.config.Config;
import com.xiaoju.automarket.paladin.core.common.JTuple2;
import com.xiaoju.automarket.paladin.core.common.JobStatusEnum;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @Author Luogh
 * @Date 2020/12/6
 **/
@Slf4j
public class JobSchedulerImpl implements JobScheduler {

    private volatile boolean isRunning = true;

    private JobEnvironment environment;

    private JobStore jobStore;

    private ExecutorService SCHEDULER_EXECUTOR;

    private final Object JOB_LOCK = new Object();
    private final BlockingDeque<JobInstance> runningJobs = new LinkedBlockingDeque<>(10); // 任务并行个数
    private final Map<String, JTuple2<JobInstance, AtomicReference<JobExecutor>>> runningJobMap = new ConcurrentHashMap<>();


    @Override
    public void configure(Config config) {
        JobEventDispatcher eventDispatcher = new JobEventDispatcher();
        JobStore jobStore = new HeapBasedJobStoreImpl();
        jobStore.configure(config);
        JobEnvironment environment = new JobEnvironmentImpl(config, eventDispatcher, jobStore);
        ThreadFactoryBuilder threadFactory = new ThreadFactoryBuilder().setDaemon(false).setNameFormat("JOB-SCHEUDLER-%d");
        ExecutorService executor = Executors.newSingleThreadExecutor(threadFactory.build());
        executor.submit(new SchedulerThread());

        this.environment = environment;
        this.jobStore = jobStore;
        this.SCHEDULER_EXECUTOR = executor;
    }

    @Override
    public CompletableFuture<Void> cancelJob(String jobId) {
        synchronized (JOB_LOCK) {
            JTuple2<JobInstance, AtomicReference<JobExecutor>> jobTuple = runningJobMap.get(jobId);
            if (jobTuple.getV() != null) {
                JobExecutor jobExecutor = jobTuple.getV().get();
                if (jobExecutor != null) {
                    CompletableFuture<Void> future = jobExecutor.cancelJob(jobTuple.getK().getJobId());
                    return future.whenCompleteAsync((r, throwable) -> {
                        if (throwable != null) {
                            String error = String.format("cancel job:[%s] failed", jobId);
                            log.error(error, throwable);
                            throw new RuntimeException(error, throwable);
                        } else {
                            this.runningJobMap.remove(jobId);
                            this.runningJobs.remove(jobTuple.getK());
                        }
                    });
                }
            }

            return CompletableFuture.runAsync(() -> {
                this.runningJobMap.remove(jobId);
                this.runningJobs.remove(jobTuple.getK());
            });
        }
    }


    @Override
    public void shutdown() {
        this.isRunning = false;
        try {
            this.SCHEDULER_EXECUTOR.shutdown();
            if (!this.SCHEDULER_EXECUTOR.isTerminated()) {
                this.SCHEDULER_EXECUTOR.awaitTermination(5, TimeUnit.SECONDS);
            }
        } catch (InterruptedException e) {
            log.warn("job scheduler shutdown failed", e);
        }
    }

    @Override
    public JobExecutor createNewJobExecutor() {
        JobExecutor jobExecutor = new ActorBasedJobExecutorImpl();
        jobExecutor.configure(environment);
        return jobExecutor;
    }


    private final class SchedulerThread implements Runnable {

        @Override
        public void run() {
            while (isRunning) {
                try {
                    Set<JobStatusEnum> candidateJobStates = Sets.newHashSet(JobStatusEnum.SUBMITTED, JobStatusEnum.RUNNING, JobStatusEnum.DEPLOYED, JobStatusEnum.INITIALIZED);
                    List<JobInstance> candidateJobs = jobStore.getJobsInStatus(candidateJobStates, runningJobs.remainingCapacity());
                    for (JobInstance jobInstance : candidateJobs) {
                        synchronized (JOB_LOCK) {
                            if (jobInstance.getJobStatus() == JobStatusEnum.SUBMITTED) {
                                deployJob(jobInstance);
                            } else {
                                if (!runningJobMap.containsKey(jobInstance.getJobId())) {
                                    log.warn("job: [%s] in running state but not found in running queue, may occurred service restart, start redeploying job.");
                                    runningJobs.remove(jobInstance);
                                    deployJob(jobInstance);
                                }
                            }
                        }
                    }
                    Thread.sleep(TimeUnit.SECONDS.toMillis(5));
                } catch (InterruptedException e) {
                    // do nothing
                }
            }
        }

        private void deployJob(JobInstance jobInstance) {
            try {
                final JobExecutor jobExecutor = createNewJobExecutor();
                CompletableFuture<Void> completableFuture = jobExecutor.deployJob(jobInstance);
                completableFuture.whenCompleteAsync((v, throwable) -> {
                    try {
                        if (throwable != null) {
                            runningJobs.remove(jobInstance);
                            runningJobMap.remove(jobInstance.getJobId());
                            log.warn(String.format("deploy job: [%s] to executor: [%s] failed with exception:", jobInstance.getJobId(), jobExecutor.getJobExecutorId()), throwable);
                            jobStore.updateJobStatus(jobInstance.getJobId(), JobStatusEnum.FAILED, throwable);
                            throw throwable;
                        } else {
                            AtomicReference<JobExecutor> jobExecutorRef = new AtomicReference<>();
                            jobExecutorRef.set(jobExecutor);
                            JTuple2<JobInstance, AtomicReference<JobExecutor>> jobTuple = new JTuple2<>(jobInstance, jobExecutorRef);
                            runningJobs.putLast(jobInstance);
                            runningJobMap.put(jobInstance.getJobId(), jobTuple);
                            jobStore.updateJobStatus(jobInstance.getJobId(), JobStatusEnum.RUNNING, null);
                            log.info(String.format("deploy job: [%s] to executor: [%s] success", jobInstance.getJobId(), jobExecutor.getJobExecutorId()));
                        }
                    } catch (Throwable e) {
                        throw new RuntimeException(e);
                    }
                });
            } catch (Exception e) {
                throw new RuntimeException("deploy job ");
            }

        }
    }
}
