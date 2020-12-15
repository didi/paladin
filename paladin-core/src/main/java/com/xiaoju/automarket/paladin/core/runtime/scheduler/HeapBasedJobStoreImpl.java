package com.xiaoju.automarket.paladin.core.runtime.scheduler;

import com.typesafe.config.Config;
import com.xiaoju.automarket.paladin.core.common.StatusEnum;
import com.xiaoju.automarket.paladin.core.dcg.JobGraphDescriptor;
import com.xiaoju.automarket.paladin.core.runtime.job.JobStore;
import com.xiaoju.automarket.paladin.core.runtime.job.JobInstance;

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
    public void updateJobStatus(String jobId, StatusEnum jobStatus, Throwable exception) {

    }

    @Override
    public List<JobInstance> getJobsInStatus(Set<StatusEnum> jobStatusSet, int expectedSize) {
        return null;
    }
}
