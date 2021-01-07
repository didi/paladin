package com.xiaoju.automarket.paladin.core.dcg;

import java.util.List;

/**
 * @Author Luogh
 * @Date 2021/1/5
 **/
public interface VertexDescriptor<T> {

    String vertexId();

    String vertexName();

    T vertexHandler();

    DependencySelectorStrategy downstreamDependencySelectorStrategy();

    List<DependencyDescriptor> upstreamDependencies();

    List<DependencyDescriptor> downstreamDependencies();
}
