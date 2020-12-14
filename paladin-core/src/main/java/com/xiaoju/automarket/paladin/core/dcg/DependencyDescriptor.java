package com.xiaoju.automarket.paladin.core.dcg;

/**
 * @Author Luogh
 * @Date 2020/11/8
 **/
public class DependencyDescriptor {
    private final int dependencyId;
    private double weight;
    private ActionDescriptor preActionDescriptor;
    private ConditionDescriptor conditionDescriptor;
    private ActionDescriptor nextActionDescriptor;

    public DependencyDescriptor(int dependencyId) {
        this.dependencyId = dependencyId;
    }

    public int getDependencyId() {
        return dependencyId;
    }

    public double getWeight() {
        return weight;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }

    public ActionDescriptor getPreActionDescriptor() {
        return preActionDescriptor;
    }

    public void setPreActionDescriptor(ActionDescriptor preActionDescriptor) {
        this.preActionDescriptor = preActionDescriptor;
    }

    public ConditionDescriptor getConditionDescriptor() {
        return conditionDescriptor;
    }

    public void setConditionDescriptor(ConditionDescriptor conditionDescriptor) {
        this.conditionDescriptor = conditionDescriptor;
    }

    public ActionDescriptor getNextActionDescriptor() {
        return nextActionDescriptor;
    }

    public void setNextActionDescriptor(ActionDescriptor nextActionDescriptor) {
        this.nextActionDescriptor = nextActionDescriptor;
    }
}
