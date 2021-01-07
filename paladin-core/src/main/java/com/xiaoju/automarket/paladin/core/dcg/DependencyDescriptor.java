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
    private final VertexDescriptor<?> preVertexDescriptor;
    private final VertexDescriptor<?> nextVertexDescriptor;
    private final String dependencyName;

    public DependencyDescriptor(String name, ConditionHandler conditionHandler, VertexDescriptor<?> preVertexDescriptor, VertexDescriptor<?> nextVertexDescriptor) {
        Preconditions.checkArgument(conditionHandler != null, "conditionHandler");
        Preconditions.checkArgument(preVertexDescriptor != null, "preVertexDescriptor");
        Preconditions.checkArgument(nextVertexDescriptor != null, "nextVertexDescriptor");
        this.dependencyId = Util.generateUUID();
        this.dependencyName = name;
        this.conditionHandler = conditionHandler;
        this.preVertexDescriptor = preVertexDescriptor;
        this.nextVertexDescriptor = nextVertexDescriptor;
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

    public VertexDescriptor<?> getPreVertexDescriptor() {
        return preVertexDescriptor;
    }

    public VertexDescriptor<?> getNextVertexDescriptor() {
        return nextVertexDescriptor;
    }


    public DependencyDescriptorView toView() {
        return new DependencyDescriptorView(dependencyId,
                dependencyName,
                weight,
                preVertexDescriptor.vertexId(),
                preVertexDescriptor.vertexName(),
                nextVertexDescriptor.vertexId(),
                nextVertexDescriptor.vertexName()
        );
    }


    public static class DependencyDescriptorView {
        private final String dependencyId;
        private final String dependencyName;
        private final double weight;
        private final String upstreamVertexId;
        private final String upstreamVertexName;
        private final String downstreamVertexId;
        private final String downstreamVertexName;

        public DependencyDescriptorView(String dependencyId, String dependencyName, double weight, String preVertexId,
                                        String upstreamVertexName, String downstreamVertexId, String downstreamVertexName) {
            this.dependencyId = dependencyId;
            this.dependencyName = dependencyName;
            this.weight = weight;
            this.upstreamVertexId = preVertexId;
            this.upstreamVertexName = upstreamVertexName;
            this.downstreamVertexId = downstreamVertexId;
            this.downstreamVertexName = downstreamVertexName;
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

        public String getUpstreamVertexId() {
            return upstreamVertexId;
        }

        public String getUpstreamVertexName() {
            return upstreamVertexName;
        }

        public String getDownstreamVertexId() {
            return downstreamVertexId;
        }

        public String getDownstreamVertexName() {
            return downstreamVertexName;
        }
    }


}
