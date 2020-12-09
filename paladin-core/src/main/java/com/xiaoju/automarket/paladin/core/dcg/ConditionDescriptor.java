package com.xiaoju.automarket.paladin.core.dcg;

import com.xiaoju.automarket.paladin.core.common.Event;
import com.xiaoju.automarket.paladin.core.runtime.ConditionHandler;
import lombok.Getter;
import lombok.Setter;

/**
 * @Author Luogh
 * @Date 2020/11/8
 **/
@Getter
@Setter
public class ConditionDescriptor<T extends Event> {
    private Class<? extends ConditionHandler<T>> condition;
}
