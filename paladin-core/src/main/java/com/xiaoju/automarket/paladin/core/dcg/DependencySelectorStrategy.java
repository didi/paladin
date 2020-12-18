package com.xiaoju.automarket.paladin.core.dcg;

import com.xiaoju.automarket.paladin.core.dcg.DependencyDescriptor.DependencyDescriptorView;

import java.util.List;

/**
 * @Author Luogh
 * @Date 2020/12/11
 **/
public interface DependencySelectorStrategy {

    List<DependencyDescriptorView> select(BizEvent event, Iterable<DependencyDescriptorView> candidates);

}
