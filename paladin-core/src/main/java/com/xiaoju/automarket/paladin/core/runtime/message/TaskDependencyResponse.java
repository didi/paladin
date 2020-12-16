package com.xiaoju.automarket.paladin.core.runtime.message;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TaskDependencyResponse {
    private String dependencyId;
    private boolean isMatched;
    private Throwable exception;
}