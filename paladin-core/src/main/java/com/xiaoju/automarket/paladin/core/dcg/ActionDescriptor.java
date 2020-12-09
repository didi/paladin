package com.xiaoju.automarket.paladin.core.dcg;

import com.xiaoju.automarket.paladin.core.common.Event;
import com.xiaoju.automarket.paladin.core.runtime.ActionHandler;
import com.xiaoju.automarket.paladin.core.runtime.DependencySelector;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

/**
 * @Author Luogh
 * @Date 2020/11/8
 **/
@Getter
@Setter
public class ActionDescriptor<IN extends Event, OUT extends Event> {
    private final int actionId;
    private Class<? extends ActionHandler<IN, OUT>> actionHandler;
    private Class<? extends DependencySelector<OUT>> dependencySelector;


    public ActionDescriptor(int actionId) {
        this.actionId = actionId;
    }
}
