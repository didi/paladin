package com.xiaoju.automarket.paladin.core.runtime.message;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UnRegisterDependency {
    private final boolean isOutside;
    private final String dependencyId;
}