package com.xiaoju.automarket.paladin.core.runtime;

import com.xiaoju.automarket.paladin.core.common.Event;

/**
 * @Author Luogh
 * @Date 2020/12/14
 **/
public interface SinkActionHandler extends ActionHandler {
    void doAction(Event event);
}
