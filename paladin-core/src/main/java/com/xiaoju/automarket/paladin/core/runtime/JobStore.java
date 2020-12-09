package com.xiaoju.automarket.paladin.core.runtime;

import com.xiaoju.automarket.paladin.core.common.JobStatus;
import com.xiaoju.automarket.paladin.core.dcg.JobGraphDescriptor;

import java.util.List;
import java.util.Set;

/**
 * @Author Luogh
 * @Date 2020/12/9
 **/
public interface JobStore {

    void configure(JobEnvironment environment);

    JobInstance createJob(JobGraphDescriptor jobGraph);

    void updateJobStatus(String jobId, JobStatus jobStatus, Throwable exception);

    List<JobInstance> getJobsInStatus(Set<JobStatus> jobStatusSet, int expectedSize);
}
