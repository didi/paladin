package com.xiaoju.automarket.paladin.core.runtime.job;

import com.xiaoju.automarket.paladin.core.common.StatusEnum;

/**
 * @Author Luogh
 * @Date 2020/12/7
 **/
public interface JobEventListener {
    void onJobStateChanged(StatusEnum jobStatus, JobInstance jobInstance, Object attachment);
}
