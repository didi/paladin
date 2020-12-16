package com.xiaoju.automarket.paladin.core.runtime.dcg;

import com.xiaoju.automarket.paladin.core.runtime.handler.ConditionHandler;
import com.xiaoju.automarket.paladin.core.util.Util;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.Objects;

/**
 * @Author Luogh
 * @Date 2020/11/8
 **/
@Getter
@Setter
public class DependencyDescriptor {
    private final String dependencyId;
    private double weight;
    private String dependencyName;
    private ActionDescriptor preActionDescriptor;
    private ConditionDescriptor conditionDescriptor;
    private ActionDescriptor nextActionDescriptor;

    public DependencyDescriptor() {
        this.dependencyId = Util.generateUUID();
        this.dependencyName = conditionDescriptor.getConditionHandler().getName();
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

    public DependencyDescriptorView toView() {
        return new DependencyDescriptorView(dependencyId,
                dependencyName,
                weight,
                preActionDescriptor.getActionId(),
                preActionDescriptor.getActionName(),
                conditionDescriptor.getConditionHandler(),
                nextActionDescriptor.getActionId(),
                nextActionDescriptor.getActionName()
        );
    }

    @Getter
    @AllArgsConstructor
    public static class DependencyDescriptorView {
        private final String dependencyId;
        private final String dependencyName;
        private final double weight;
        private final String preActionId;
        private final String preActionName;
        private final Class<? extends ConditionHandler> conditionHandler;
        private final String nextActionId;
        private final String nextActionName;
    }
}
