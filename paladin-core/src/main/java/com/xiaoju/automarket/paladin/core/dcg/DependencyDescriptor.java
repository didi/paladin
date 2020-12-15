package com.xiaoju.automarket.paladin.core.dcg;

import com.xiaoju.automarket.paladin.core.runtime.handler.ConditionHandler;
import lombok.AllArgsConstructor;
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
    private ActionDescriptor preActionDescriptor;
    private ConditionDescriptor conditionDescriptor;
    private ActionDescriptor nextActionDescriptor;

    public DependencyDescriptor(int dependencyId) {
        this.dependencyId = dependencyId;
    }

    public DependencyDescriptorView toView() {
        return new DependencyDescriptorView(dependencyId, weight,
                preActionDescriptor.getActionId(),
                conditionDescriptor.getConditionHandler(),
                nextActionDescriptor.getActionId()
        );
    }

    @Getter
    @AllArgsConstructor
    public static class DependencyDescriptorView {
        private final int dependencyId;
        private final double weight;
        private final int preActionId;
        private final Class<? extends ConditionHandler> conditionHandler;
        private final int nextActionId;
    }
}
