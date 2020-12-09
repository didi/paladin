package com.xiaoju.automarket.paladin.core.dcg;

import com.xiaoju.automarket.paladin.core.common.Event;
import lombok.Getter;
import lombok.Setter;

/**
 * @Author Luogh
 * @Date 2020/11/8
 **/
@Getter
@Setter
public class DependencyDescriptor<T extends Event> {
    private final int dependencyId;
    private double weight;
    private ActionDescriptor<T, ? extends Event> preAction;
    private ConditionDescriptor<T> condition;
    private ActionDescriptor<T, ? extends Event> nextAction;

    public DependencyDescriptor(int dependencyId) {
        this.dependencyId = dependencyId;
    }
}
