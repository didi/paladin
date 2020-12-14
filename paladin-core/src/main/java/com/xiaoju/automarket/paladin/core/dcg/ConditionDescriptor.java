package com.xiaoju.automarket.paladin.core.dcg;

import com.xiaoju.automarket.paladin.core.runtime.ConditionHandler;

/**
 * @Author Luogh
 * @Date 2020/11/8
 **/
public class ConditionDescriptor {
    private Class<? extends ConditionHandler> conditionHandler;

    public Class<? extends ConditionHandler> getConditionHandler() {
        return conditionHandler;
    }

    public void setConditionHandler(Class<? extends ConditionHandler> conditionHandler) {
        this.conditionHandler = conditionHandler;
    }
}
