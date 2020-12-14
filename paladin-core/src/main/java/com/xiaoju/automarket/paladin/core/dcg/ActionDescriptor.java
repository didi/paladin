package com.xiaoju.automarket.paladin.core.dcg;

import com.xiaoju.automarket.paladin.core.runtime.ActionHandler;
import com.xiaoju.automarket.paladin.core.runtime.DependencySelector;

import java.util.List;

/**
 * @Author Luogh
 * @Date 2020/11/8
 **/
public class ActionDescriptor {
    private final int actionId;
    private Class<? extends ActionHandler> actionHandler;
    private List<DependencyDescriptor> downstreamDependencies;
    private Class<? extends DependencySelector> dependencySelector;

    public ActionDescriptor(int actionId) {
        this.actionId = actionId;
    }

    public int getActionId() {
        return actionId;
    }

    public Class<? extends ActionHandler> getActionHandler() {
        return actionHandler;
    }

    public void setActionHandler(Class<? extends ActionHandler> actionHandler) {
        this.actionHandler = actionHandler;
    }

    public List<DependencyDescriptor> getDownstreamDependencies() {
        return downstreamDependencies;
    }

    public void setDownstreamDependencies(List<DependencyDescriptor> downstreamDependencies) {
        this.downstreamDependencies = downstreamDependencies;
    }

    public Class<? extends DependencySelector> getDependencySelector() {
        return dependencySelector;
    }

    public void setDependencySelector(Class<? extends DependencySelector> dependencySelector) {
        this.dependencySelector = dependencySelector;
    }
}
