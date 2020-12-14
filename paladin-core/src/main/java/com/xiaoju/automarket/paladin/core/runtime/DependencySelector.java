package com.xiaoju.automarket.paladin.core.runtime;

import com.xiaoju.automarket.paladin.core.common.Event;
import com.xiaoju.automarket.paladin.core.dcg.DependencyDescriptor;

import java.util.List;

/**
 * @Author Luogh
 * @Date 2020/12/11
 **/
public interface DependencySelector {

    List<DependencyDescriptor> select(Event event, List<DependencyDescriptor> candidates);
}
