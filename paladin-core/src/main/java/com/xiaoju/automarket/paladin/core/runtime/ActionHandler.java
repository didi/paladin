package com.xiaoju.automarket.paladin.core.runtime;

import com.typesafe.config.Config;
import com.xiaoju.automarket.paladin.core.common.Event;

import java.util.List;

/**
 * @Author Luogh
 * @Date 2020/11/8
 **/
public interface ActionHandler {

    void configure(Config config);

    ActionResult doAction(Event event);

    void destroy();
}
