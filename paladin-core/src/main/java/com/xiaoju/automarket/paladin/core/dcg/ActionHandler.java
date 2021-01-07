package com.xiaoju.automarket.paladin.core.dcg;

import com.xiaoju.automarket.paladin.core.runtime.common.Configuration;

import java.time.Duration;

/**
 * @Author Luogh
 * @Date 2020/12/14
 **/
public interface ActionHandler {
    void initialize(Configuration config);

    void doAction(BizEvent input, Collector collector);

    void stop();

    interface Collector {
        void collect(BizEvent out, Duration fireDuration);

        void collect(BizEvent out);

        void onCallback(String callbackId, ActionCallbackHandler callbackHandler, Duration timeout);
    }

    interface ActionCallbackHandler {
        void handle(BizCallbackEvent event);

        void onTimeout();
    }
}
