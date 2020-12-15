package com.xiaoju.automarket.paladin.core.runtime.job;

import com.xiaoju.automarket.paladin.core.common.StatusEnum;

/**
 * @Author Luogh
 * @Date 2020/12/8
 **/
public interface JobEventPublisher {
    void publish(StatusEnum status, JobInstance jobInstance, Object attachment);

    void addListener(JobEventListener listener);

    void removeListener(JobEventListener listener);
}
