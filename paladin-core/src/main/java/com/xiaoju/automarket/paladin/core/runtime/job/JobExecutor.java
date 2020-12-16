package com.xiaoju.automarket.paladin.core.runtime.job;

/**
 * @Author Luogh
 * @Date 2020/12/6
 **/
public interface JobExecutor {

    void configure(JobEnvironment environment);

    String getJobExecutorId();

    void deployJob(JobInstance jobInstance);

    void cancelJob(String jobId);
}
