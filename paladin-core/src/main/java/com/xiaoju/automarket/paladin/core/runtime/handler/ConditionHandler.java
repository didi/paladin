package com.xiaoju.automarket.paladin.core.runtime.handler;

import com.typesafe.config.Config;
import com.xiaoju.automarket.paladin.core.runtime.message.SubscriptionEvent;

/**
 * @Author Luogh
 * @Date 2020/11/8
 **/
public interface ConditionHandler {
    void initialize(Config config);

    boolean doCheck(SubscriptionEvent event);
}
