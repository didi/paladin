package com.xiaoju.automarket.paladin.core.runtime.job;

import java.util.concurrent.CompletableFuture;

/**
 * @Author Luogh
 * @Date 2020/12/6
 **/
public interface JobExecutor {

    void configure(JobEnvironment environment);

    String getJobExecutorId();

    CompletableFuture<Void> deployJob(JobInstance jobInstance);

    CompletableFuture<Void> cancelJob(String jobId);

}
