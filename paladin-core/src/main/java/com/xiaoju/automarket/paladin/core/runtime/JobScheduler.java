package com.xiaoju.automarket.paladin.core.runtime;

import com.typesafe.config.Config;

import java.util.concurrent.CompletableFuture;

/**
 * @Author Luogh
 * @Date 2020/12/6
 **/
public interface JobScheduler {

    void configure(Config config);

    CompletableFuture<Void> cancelJob(String jobId);

    void shutdown();

    JobExecutor createNewJobExecutor();
}
