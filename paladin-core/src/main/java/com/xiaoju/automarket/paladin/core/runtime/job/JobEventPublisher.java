package com.xiaoju.automarket.paladin.core.runtime.job;

import com.xiaoju.automarket.paladin.core.common.ExecutionStateEnum;

/**
 * @Author Luogh
 * @Date 2020/12/8
 **/
public interface JobEventPublisher {
    void publish(ExecutionStateEnum status, JobInstance jobInstance, Object attachment);

    void addListener(JobEventListener listener);

    void removeListener(JobEventListener listener);
}
