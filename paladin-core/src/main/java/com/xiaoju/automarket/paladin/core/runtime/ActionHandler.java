package com.xiaoju.automarket.paladin.core.runtime;

import com.typesafe.config.Config;
import com.xiaoju.automarket.paladin.core.common.Event;

/**
 * @Author Luogh
 * @Date 2020/11/8
 **/
public interface ActionHandler<IN extends Event, OUT extends Event> {

    void configure(Config config);

    void beforeAction(IN event);

    void doAction(IN event, ActionContext<OUT> actionContext);

    void destroy();

    interface ActionContext<OUT> {
        void collect(OUT event);

        Context context();
    }
}
