package com.xiaoju.automarket.paladin.core.dcg;

import lombok.Getter;
import lombok.Setter;

/**
 * @Author Luogh
 * @Date 2020/11/8
 **/
@Getter
@Setter
public class DependencyDescriptor {
    private final int dependencyId;
    private double weight;
    private ActionDescriptor preAction;
    private ConditionDescriptor condition;
    private ActionDescriptor nextAction;

    public DependencyDescriptor(int dependencyId) {
        this.dependencyId = dependencyId;
    }
}
