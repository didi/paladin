package com.xiaoju.automarket.paladin.core.runtime;

import com.xiaoju.automarket.paladin.core.common.Event;

/**
 * @Author Luogh
 * @Date 2020/11/8
 **/
public interface TransformActionHandler extends ActionHandler {
    ActionResult doAction(Event event);
}
