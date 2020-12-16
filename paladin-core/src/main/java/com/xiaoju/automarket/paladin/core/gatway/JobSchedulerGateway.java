package com.xiaoju.automarket.paladin.core.gatway;

import com.xiaoju.automarket.paladin.core.common.Job;
import com.xiaoju.automarket.paladin.core.runtime.dcg.JobGraphDescriptor;

/**
 * @Author Luogh
 * @Date 2020/12/16
 **/
public interface JobSchedulerGateway {
    Job submitJob(JobGraphDescriptor jobGraphDescriptor);

    Job cancelJob(String jobId);
}
