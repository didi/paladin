package com.xiaoju.automarket.paladin.core.runtime;

import com.typesafe.config.Config;

/**
 * @Author Luogh
 * @Date 2020/12/8
 **/
public final class JobEnvironmentImpl implements JobEnvironment {

    private final Config config;
    private final JobEventDispatcher jobEventDispatcher;

    public JobEnvironmentImpl(Config config, JobEventDispatcher jobEventDispatcher) {
        this.config = config;
        this.jobEventDispatcher = jobEventDispatcher;
    }


    @Override
    public Config configuration() {
        return this.config;
    }

    @Override
    public JobEventDispatcher eventDispatcher() {
        return this.jobEventDispatcher;
    }
}
