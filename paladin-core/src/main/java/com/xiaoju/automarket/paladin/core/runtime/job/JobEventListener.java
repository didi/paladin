package com.xiaoju.automarket.paladin.core.runtime.job;

import com.xiaoju.automarket.paladin.core.common.ExecutionStateEnum;

/**
 * @Author Luogh
 * @Date 2020/12/7
 **/
public interface JobEventListener {
    void onJobStateChanged(ExecutionStateEnum jobStatus, JobInstance jobInstance, Object attachment);
}
