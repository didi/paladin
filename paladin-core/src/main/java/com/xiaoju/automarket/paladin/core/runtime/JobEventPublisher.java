package com.xiaoju.automarket.paladin.core.runtime;

import com.xiaoju.automarket.paladin.core.common.JobStatus;

/**
 * @Author Luogh
 * @Date 2020/12/8
 **/
public interface JobEventPublisher {
    void publish(JobStatus status, JobInstance jobInstance, Object attachment);

    void addListener(JobEventListener listener);

    void removeListener(JobEventListener listener);
}
