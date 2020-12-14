package com.xiaoju.automarket.paladin.core.dcg;

import java.util.List;
import java.util.Map;

/**
 * @Author Luogh
 * @Date 2020/11/8
 **/
public class JobGraphDescriptor {
    private Map<Integer, ActionDescriptor> actions;
    private Map<Integer, DependencyDescriptor> dependencies;
    private Map<Integer, List<Integer>> action2DependencyMap;
    /*图中所有入度为0的vertex*/
    private List<Integer> actionSources;

    public Map<Integer, DependencyDescriptor> getDependencies() {
        return dependencies;
    }

    public void setDependencies(Map<Integer, DependencyDescriptor> dependencies) {
        this.dependencies = dependencies;
    }

    public Map<Integer, List<Integer>> getAction2DependencyMap() {
        return action2DependencyMap;
    }

    public void setAction2DependencyMap(Map<Integer, List<Integer>> action2DependencyMap) {
        this.action2DependencyMap = action2DependencyMap;
    }

    public List<Integer> getActionSources() {
        return actionSources;
    }

    public Map<Integer, ActionDescriptor> getActions() {
        return actions;
    }
}
