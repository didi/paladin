package com.xiaoju.automarket.paladin.core.runtime.dcg;

import com.xiaoju.automarket.paladin.core.runtime.handler.ActionHandler;
import com.xiaoju.automarket.paladin.core.runtime.handler.DependencySelectorStrategy;
import com.xiaoju.automarket.paladin.core.util.Util;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Objects;

/**
 * @Author Luogh
 * @Date 2020/11/8
 **/
@Getter
@Setter
public class ActionDescriptor {
    private final String actionId;
    private String actionName;
    private List<DependencyDescriptor> upstreamDependencies;
    private Class<? extends ActionHandler> actionHandler;
    private List<DependencyDescriptor> downstreamDependencies;
    private Class<? extends DependencySelectorStrategy> dependencySelectorStrategy;

    public ActionDescriptor() {
        this.actionId = Util.generateUUID();
        this.actionName = actionHandler.getName();
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
}
