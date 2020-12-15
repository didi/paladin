package com.xiaoju.automarket.paladin.core.runtime.job;

import com.xiaoju.automarket.paladin.core.common.StatusEnum;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @Author Luogh
 * @Date 2020/12/8
 **/
@Slf4j
public class JobEventDispatcher implements JobEventPublisher {

    private final CopyOnWriteArrayList<JobEventListener> listeners = new CopyOnWriteArrayList<>();

    @Override
    public void publish(StatusEnum status, JobInstance jobInstance, Object attachment) {
        for (JobEventListener jobEventListener : listeners) {
            jobEventListener.onJobStateChanged(status, jobInstance, attachment);
        }
    }

    @Override
    public void addListener(JobEventListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
            log.info("register listener: {}, total listeners: {}", listener, listeners.size());
        }
    }

    @Override
    public void removeListener(JobEventListener listener) {
        listeners.remove(listener);
    }
}
