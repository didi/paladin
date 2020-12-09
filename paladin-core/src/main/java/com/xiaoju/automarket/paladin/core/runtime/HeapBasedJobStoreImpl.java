package com.xiaoju.automarket.paladin.core.runtime;

import com.xiaoju.automarket.paladin.core.common.JobStatus;
import com.xiaoju.automarket.paladin.core.dcg.JobGraphDescriptor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Author Luogh
 * @Date 2020/12/9
 **/
public class HeapBasedJobStoreImpl implements JobStore {

    private final Map<String, JobInstance> ALL_JOBS = new ConcurrentHashMap<>();

    @Override
    public void configure(JobEnvironment environment) {

    }

    @Override
    public JobInstance createJob(JobGraphDescriptor jobGraph) {
        return null;
    }

    @Override
    public void updateJobStatus(String jobId, JobStatus jobStatus, Throwable exception) {

    }

    @Override
    public List<JobInstance> getJobsInStatus(Set<JobStatus> jobStatusSet, int expectedSize) {
        return null;
    }
}
