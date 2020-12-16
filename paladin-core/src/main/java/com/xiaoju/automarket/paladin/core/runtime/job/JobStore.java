package com.xiaoju.automarket.paladin.core.runtime.job;

import com.typesafe.config.Config;
import com.xiaoju.automarket.paladin.core.common.ExecutionStateEnum;
import com.xiaoju.automarket.paladin.core.runtime.dcg.JobGraphDescriptor;

import java.util.List;
import java.util.Set;

/**
 * @Author Luogh
 * @Date 2020/12/9
 **/
public interface JobStore {

    void configure(Config configuration);

    JobInstance createJob(JobGraphDescriptor jobGraph);

    void updateJobStatus(String jobId, ExecutionStateEnum jobStatus, Throwable exception);

    List<JobInstance> getJobsInStatus(Set<ExecutionStateEnum> jobStatusSet, int expectedSize);
}
