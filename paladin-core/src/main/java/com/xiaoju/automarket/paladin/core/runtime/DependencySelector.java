package com.xiaoju.automarket.paladin.core.runtime;

import com.xiaoju.automarket.paladin.core.common.Event;
import com.xiaoju.automarket.paladin.core.dcg.DependencyDescriptor;

import java.util.List;

/**
 * @Author Luogh
 * @Date 2020/11/8
 **/
public interface DependencySelector<T extends Event> {

    List<DependencyDescriptor<T>> selectNextDependencies(T event, Context context);
}
