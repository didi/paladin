package com.xiaoju.automarket.paladin.core.runtime;

import com.typesafe.config.Config;
import com.xiaoju.automarket.paladin.core.common.JobStatusEnum;
import com.xiaoju.automarket.paladin.core.dcg.JobGraphDescriptor;

import java.util.List;
import java.util.Set;

/**
 * @Author Luogh
 * @Date 2020/12/9
 **/
public interface JobStore {

    void configure(Config configuration);

    JobInstance createJob(JobGraphDescriptor jobGraph);

    void updateJobStatus(String jobId, JobStatusEnum jobStatus, Throwable exception);

    List<JobInstance> getJobsInStatus(Set<JobStatusEnum> jobStatusSet, int expectedSize);
}
