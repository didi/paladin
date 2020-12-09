package com.xiaoju.automarket.paladin.core.runtime;

import com.typesafe.config.Config;
import com.xiaoju.automarket.paladin.core.common.Event;

/**
 * @Author Luogh
 * @Date 2020/11/8
 **/
public interface ConditionHandler<T extends Event> {
    void configure(Config config);

    boolean doCheck(T event, ConditionContext<T> context);

    void destroy();

    interface ConditionContext<T> {
        Context context();
    }
}
