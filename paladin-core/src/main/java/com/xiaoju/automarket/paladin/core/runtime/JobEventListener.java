package com.xiaoju.automarket.paladin.core.runtime;

import com.xiaoju.automarket.paladin.core.common.JobStatus;

/**
 * @Author Luogh
 * @Date 2020/12/7
 **/
public interface JobEventListener {
    void onJobStateChanged(JobStatus jobStatus, JobInstance jobInstance, Object attachment);
}
