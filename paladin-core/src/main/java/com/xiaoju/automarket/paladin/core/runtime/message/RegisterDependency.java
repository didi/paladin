package com.xiaoju.automarket.paladin.core.runtime.message;

import akka.actor.ActorRef;
import com.xiaoju.automarket.paladin.core.runtime.dcg.DependencyDescriptor;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class RegisterDependency {
    private final boolean isOutside;
    private final String dependencyId;
    private final ActorRef taskRef;
    private final DependencyDescriptor.DependencyDescriptorView dependency;
}