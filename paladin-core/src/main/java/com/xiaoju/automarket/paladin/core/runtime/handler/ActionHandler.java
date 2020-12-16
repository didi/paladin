package com.xiaoju.automarket.paladin.core.runtime.handler;

import com.typesafe.config.Config;
import com.xiaoju.automarket.paladin.core.runtime.message.SubscriptionEvent;
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

    List<Class<SubscriptionEvent>> subscribeEventTypes();

    boolean isEventMatched(SubscriptionEvent event);

    ActionResult doAction(SubscriptionEvent event);

    void destroy();

    @Getter
    @Setter
    class ActionResult {
        private SubscriptionEvent event;
        private Duration fireDuration;
    }
}
