package com.xiaoju.automarket.paladin.core.dcg;

import java.util.List;
import java.util.Map;

/**
 * @Author Luogh
 * @Date 2020/11/8
 **/
public class JobGraphDescriptor {
    private List<SourceDescriptor> sources;
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

    public List<SourceDescriptor> getSources() {
        return sources;
    }

    public void setSources(List<SourceDescriptor> sources) {
        this.sources = sources;
    }
}
