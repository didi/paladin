package com.xiaoju.automarket.paladin.core.runtime.handler;

import com.typesafe.config.Config;
import com.xiaoju.automarket.paladin.core.runtime.task.Messages;
import lombok.Getter;
import lombok.Setter;

import java.time.Duration;
import java.util.List;

/**
 * @Author Luogh
 * @Date 2020/12/14
 **/
public interface ActionHandler {
    void initialize(Config config);

    List<Class<Messages.SubscriptionEvent>> subscribeEventTypes();

    boolean isEventMatched(Messages.SubscriptionEvent event);

    ActionResult doAction(Messages.SubscriptionEvent event);

    void destroy();

    @Getter
    @Setter
    class ActionResult {
        private Messages.SubscriptionEvent event;
        private Duration fireDuration;
    }
}
