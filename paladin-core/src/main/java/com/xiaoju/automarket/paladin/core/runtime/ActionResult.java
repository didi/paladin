package com.xiaoju.automarket.paladin.core.runtime;

import com.xiaoju.automarket.paladin.core.common.Event;

import java.time.Duration;

/**
 * @Author Luogh
 * @Date 2020/12/11
 **/
public class ActionResult {
    private final boolean isSuccess;
    private final Event event;
    private final Duration fireDuration;

    public ActionResult(boolean isSuccess, Event event, Duration fireDuration) {
        this.isSuccess = isSuccess;
        this.event = event;
        this.fireDuration = fireDuration;
    }

    public boolean isSuccess() {
        return isSuccess;
    }

    public Event getEvent() {
        return event;
    }

    public Duration getFireDuration() {
        return fireDuration;
    }
}
