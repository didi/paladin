package com.xiaoju.automarket.paladin.core.dcg;

import java.util.Map;

/**
 * @Author Luogh
 * @Date 2020/12/31
 **/
public class BizCallbackEvent {
    private String callbackId;
    private long eventTime;
    private Map<String, Object> fields;

    public long getEventTime() {
        return eventTime;
    }

    public void setEventTime(long eventTime) {
        this.eventTime = eventTime;
    }

    public Map<String, Object> getFields() {
        return fields;
    }

    public void setFields(Map<String, Object> fields) {
        this.fields = fields;
    }

    public String getCallbackId() {
        return callbackId;
    }

    public void setCallbackId(String callbackId) {
        this.callbackId = callbackId;
    }
}
