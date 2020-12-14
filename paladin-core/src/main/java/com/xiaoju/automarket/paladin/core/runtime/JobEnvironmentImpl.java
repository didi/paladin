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

    public JobEnvironmentImpl(Config config, JobEventDispatcher jobEventDispatcher, JobStore jobStore) {
        this.config = config;
        this.jobEventDispatcher = jobEventDispatcher;
        this.jobStore = jobStore;
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
}
