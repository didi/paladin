package com.xiaoju.automarket.paladin.core.dcg;

import com.google.common.base.Preconditions;
import com.xiaoju.automarket.paladin.core.util.Util;

import java.util.List;

/**
 * @Author Luogh
 * @Date 2020/12/31
 **/
public class SourceDescriptor implements VertexDescriptor<SourceHandler> {
    private final String sourceId;
    private final SourceHandler sourceHandler;

    private String sourceName;
    private List<DependencyDescriptor> downstreamDependencies;
    private DependencySelectorStrategy dependencySelectorStrategy;

    public SourceDescriptor(String sourceName, SourceHandler sourceHandler) {
        Preconditions.checkArgument(sourceHandler != null, "SourceHandler not null");
        this.sourceId = Util.generateUUID();
        this.sourceName = sourceName;
        this.sourceHandler = sourceHandler;

    }

    public void setSourceName(String sourceName) {
        this.sourceName = sourceName;
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

    @Override
    public String vertexId() {
        return sourceId;
    }

    @Override
    public String vertexName() {
        return sourceName;
    }

    @Override
    public SourceHandler vertexHandler() {
        return sourceHandler;
    }

    @Override
    public DependencySelectorStrategy downstreamDependencySelectorStrategy() {
        return dependencySelectorStrategy;
    }

    @Override
    public List<DependencyDescriptor> upstreamDependencies() {
        return null;
    }

    @Override
    public List<DependencyDescriptor> downstreamDependencies() {
        return downstreamDependencies;
    }
}
