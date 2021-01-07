package com.xiaoju.automarket.paladin.core.dcg;

import com.xiaoju.automarket.paladin.core.runtime.common.Configuration;

/**
 * @Author Luogh
 * @Date 2020/12/31
 **/
public interface SourceHandler {
    void initialize(Configuration config);

    boolean hashNext();

    BizEvent next();

    void stop();

}
