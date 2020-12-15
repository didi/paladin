package com.xiaoju.automarket.paladin.core.dcg;

import com.xiaoju.automarket.paladin.core.runtime.handler.ActionHandler;
import com.xiaoju.automarket.paladin.core.runtime.handler.DependencySelectorStrategy;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * @Author Luogh
 * @Date 2020/11/8
 **/
@Getter
@Setter
public class ActionDescriptor {
    private final int actionId;
    private Class<? extends ActionHandler> actionHandler;
    private List<DependencyDescriptor> downstreamDependencies;
    private Class<? extends DependencySelectorStrategy> dependencySelectorStrategy;

    public ActionDescriptor(int actionId) {
        this.actionId = actionId;
    }
}
