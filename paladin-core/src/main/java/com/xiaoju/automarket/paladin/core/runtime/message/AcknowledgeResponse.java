package com.xiaoju.automarket.paladin.core.runtime.message;

/**
 * @Author Luogh
 * @Date 2020/12/16
 **/
public class AcknowledgeResponse {
    private static final AcknowledgeResponse INSTANCE = new AcknowledgeResponse();

    private AcknowledgeResponse() {}

    public static AcknowledgeResponse getInstance() {
        return INSTANCE;
    }
}
