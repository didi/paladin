package com.xiaoju.automarket.paladin.core.runtime;

import com.typesafe.config.Config;
import com.xiaoju.automarket.paladin.core.common.JobStatusEnum;
import com.xiaoju.automarket.paladin.core.dcg.JobGraphDescriptor;

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
    public void configure(Config configuration) {

    }

    @Override
    public JobInstance createJob(JobGraphDescriptor jobGraph) {
        return null;
    }

    @Override
    public void updateJobStatus(String jobId, JobStatusEnum jobStatus, Throwable exception) {

    }

    @Override
    public List<JobInstance> getJobsInStatus(Set<JobStatusEnum> jobStatusSet, int expectedSize) {
        return null;
    }
}
