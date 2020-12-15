package com.xiaoju.automarket.paladin.core.runtime.handler;

import com.xiaoju.automarket.paladin.core.dcg.DependencyDescriptor.DependencyDescriptorView;
import com.xiaoju.automarket.paladin.core.runtime.task.Messages;

import java.util.List;

/**
 * @Author Luogh
 * @Date 2020/12/11
 **/
public interface DependencySelectorStrategy {

    List<DependencyDescriptorView> select(Messages.SubscriptionEvent event, List<DependencyDescriptorView> candidates);

}
