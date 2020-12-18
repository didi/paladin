package com.xiaoju.automarket.paladin.core.dcg;

import com.google.common.base.Preconditions;
import com.xiaoju.automarket.paladin.core.util.Util;

import java.util.Objects;

/**
 * @Author Luogh
 * @Date 2020/11/8
 **/
public class DependencyDescriptor {
    private final String dependencyId;
    private final ConditionHandler conditionHandler;
    private double weight;
    private String dependencyName;
    private ActionDescriptor preActionDescriptor;
    private ActionDescriptor nextActionDescriptor;

    public DependencyDescriptor(ConditionHandler conditionHandler) {
        Preconditions.checkArgument(conditionHandler != null, "conditionHandler");
        this.dependencyId = Util.generateUUID();
        this.dependencyName = conditionHandler.getClass().getName();
        this.conditionHandler = conditionHandler;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DependencyDescriptor that = (DependencyDescriptor) o;
        return Objects.equals(dependencyId, that.dependencyId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(dependencyId);
    }

    public String getDependencyId() {
        return dependencyId;
    }

    public ConditionHandler getConditionHandler() {
        return conditionHandler;
    }

    public double getWeight() {
        return weight;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }

    public String getDependencyName() {
        return dependencyName;
    }

    public void setDependencyName(String dependencyName) {
        this.dependencyName = dependencyName;
    }

    public ActionDescriptor getPreActionDescriptor() {
        return preActionDescriptor;
    }

    public void setPreActionDescriptor(ActionDescriptor preActionDescriptor) {
        this.preActionDescriptor = preActionDescriptor;
    }

    public ActionDescriptor getNextActionDescriptor() {
        return nextActionDescriptor;
    }

    public void setNextActionDescriptor(ActionDescriptor nextActionDescriptor) {
        this.nextActionDescriptor = nextActionDescriptor;
    }

    public DependencyDescriptorView toView() {
        return new DependencyDescriptorView(dependencyId,
                dependencyName,
                weight,
                preActionDescriptor.getActionId(),
                preActionDescriptor.getActionName(),
                nextActionDescriptor.getActionId(),
                nextActionDescriptor.getActionName()
        );
    }


    public static class DependencyDescriptorView {
        private final String dependencyId;
        private final String dependencyName;
        private final double weight;
        private final String upstreamActionId;
        private final String upstreamActionName;
        private final String downstreamActionId;
        private final String downstreamActionName;

        public DependencyDescriptorView(String dependencyId, String dependencyName, double weight, String preActionId, String upstreamActionName, String downstreamActionId, String downstreamActionName) {
            this.dependencyId = dependencyId;
            this.dependencyName = dependencyName;
            this.weight = weight;
            this.upstreamActionId = preActionId;
            this.upstreamActionName = upstreamActionName;
            this.downstreamActionId = downstreamActionId;
            this.downstreamActionName = downstreamActionName;
        }

        public String getDependencyId() {
            return dependencyId;
        }

        public String getDependencyName() {
            return dependencyName;
        }

        public double getWeight() {
            return weight;
        }

        public String getUpstreamActionId() {
            return upstreamActionId;
        }

        public String getUpstreamActionName() {
            return upstreamActionName;
        }

        public String getDownstreamActionId() {
            return downstreamActionId;
        }

        public String getDownstreamActionName() {
            return downstreamActionName;
        }
    }


}
