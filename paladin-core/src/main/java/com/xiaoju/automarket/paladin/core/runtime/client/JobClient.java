package com.xiaoju.automarket.paladin.core.runtime.client;

import com.xiaoju.automarket.paladin.core.common.Job;
import com.xiaoju.automarket.paladin.core.dcg.JobGraphDescriptor;

/**
 * @Author Luogh
 * @Date 2020/12/9
 **/
public interface JobClient {
    Job submitJob(JobGraphDescriptor graphDescriptor);

    Boolean killJob(String jobId);
}
