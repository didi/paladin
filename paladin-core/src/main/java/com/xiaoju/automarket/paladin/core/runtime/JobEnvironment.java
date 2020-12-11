package com.xiaoju.automarket.paladin.core.runtime;

import akka.actor.ActorSystem;
import com.typesafe.config.Config;

/**
 * @Author Luogh
 * @Date 2020/12/8
 **/
public interface JobEnvironment {
    Config configuration();

    JobEventDispatcher eventDispatcher();

    JobStore jobStore();

    ActorSystem actorSystem();
}
