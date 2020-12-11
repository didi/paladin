package com.xiaoju.automarket.paladin.core.runtime;

import akka.actor.ActorSystem;
import com.typesafe.config.Config;

/**
 * @Author Luogh
 * @Date 2020/12/8
 **/
public final class JobEnvironmentImpl implements JobEnvironment {

    private final Config config;
    private final JobEventDispatcher jobEventDispatcher;
    private final JobStore jobStore;
    private final ActorSystem actorSystem;

    public JobEnvironmentImpl(Config config, JobEventDispatcher jobEventDispatcher, JobStore jobStore, ActorSystem actorSystem) {
        this.config = config;
        this.jobEventDispatcher = jobEventDispatcher;
        this.jobStore = jobStore;
        this.actorSystem = actorSystem;
    }

    @Override
    public Config configuration() {
        return this.config;
    }

    @Override
    public JobEventDispatcher eventDispatcher() {
        return this.jobEventDispatcher;
    }

    @Override
    public JobStore jobStore() {
        return this.jobStore;
    }

    @Override
    public ActorSystem actorSystem() {
        return this.actorSystem;
    }
}
