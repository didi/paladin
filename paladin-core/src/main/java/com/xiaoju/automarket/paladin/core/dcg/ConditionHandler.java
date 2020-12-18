package com.xiaoju.automarket.paladin.core.dcg;

import com.xiaoju.automarket.paladin.core.runtime.common.Configuration;

/**
 * @Author Luogh
 * @Date 2020/11/8
 **/
public interface ConditionHandler {
    void initialize(Configuration config);

    boolean test(BizEvent event);

    void stop();
}
