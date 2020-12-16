package com.xiaoju.automarket.paladin.core.runtime.message;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TaskDependencyCheckRequest {
    private SubscriptionEvent event;
}