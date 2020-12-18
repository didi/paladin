package com.xiaoju.automarket.paladin.core.dcg;

import com.google.common.base.Preconditions;
import com.xiaoju.automarket.paladin.core.util.Util;

import java.util.List;
import java.util.Objects;

/**
 * @Author Luogh
 * @Date 2020/11/8
 **/

public class ActionDescriptor {
    private final String actionId;
    private final ActionHandler actionHandler;

    private String actionName;
    private List<DependencyDescriptor> upstreamDependencies;
    private List<DependencyDescriptor> downstreamDependencies;
    private DependencySelectorStrategy dependencySelectorStrategy;

    public ActionDescriptor(String actionName, ActionHandler actionHandler) {
        Preconditions.checkArgument(actionHandler != null, "ActionHandler not null");
        this.actionId = Util.generateUUID();
        this.actionName = actionName;
        this.actionHandler = actionHandler;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ActionDescriptor that = (ActionDescriptor) o;
        return Objects.equals(actionId, that.actionId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(actionId);
    }

    public String getActionId() {
        return actionId;
    }

    public ActionHandler getActionHandler() {
        return actionHandler;
    }

    public String getActionName() {
        return actionName;
    }

    public void setActionName(String actionName) {
        this.actionName = actionName;
    }

    public List<DependencyDescriptor> getUpstreamDependencies() {
        return upstreamDependencies;
    }

    public void setUpstreamDependencies(List<DependencyDescriptor> upstreamDependencies) {
        this.upstreamDependencies = upstreamDependencies;
    }

    public List<DependencyDescriptor> getDownstreamDependencies() {
        return downstreamDependencies;
    }

    public void setDownstreamDependencies(List<DependencyDescriptor> downstreamDependencies) {
        this.downstreamDependencies = downstreamDependencies;
    }

    public DependencySelectorStrategy getDependencySelectorStrategy() {
        return dependencySelectorStrategy;
    }

    public void setDependencySelectorStrategy(DependencySelectorStrategy dependencySelectorStrategy) {
        this.dependencySelectorStrategy = dependencySelectorStrategy;
    }
}
