package com.xiaoju.automarket.paladin.core.runtime.message;

import akka.actor.ActorRef;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class RegisterAction {
    private final String actionId;
    private final String actionName;
    private final ActorRef taskRef;
}