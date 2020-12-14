package com.xiaoju.automarket.paladin.core.runtime;

import com.xiaoju.automarket.paladin.core.common.JobStatusEnum;

/**
 * @Author Luogh
 * @Date 2020/12/7
 **/
public interface JobEventListener {
    void onJobStateChanged(JobStatusEnum jobStatus, JobInstance jobInstance, Object attachment);
}
