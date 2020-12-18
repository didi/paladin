package com.xiaoju.automarket.paladin.core.dcg;

import lombok.Getter;

import java.util.Map;

/**
 * @Author Luogh
 * @Date 2020/11/8
 **/
public class JobGraphDescriptor {
    private Map<String, ActionDescriptor> actions;
    private Map<String, DependencyDescriptor> dependencies;

    public Map<String, ActionDescriptor> getActions() {
        return actions;
    }

    public void setActions(Map<String, ActionDescriptor> actions) {
        this.actions = actions;
    }

    public Map<String, DependencyDescriptor> getDependencies() {
        return dependencies;
    }

    public void setDependencies(Map<String, DependencyDescriptor> dependencies) {
        this.dependencies = dependencies;
    }
}
