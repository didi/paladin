package com.xiaoju.automarket.paladin.core.common;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;

/**
 * @Author Luogh
 * @Date 2020/11/7
 **/
@Getter
@Setter
public class Event {
    private String eventId;
    private String eventType;
    private long pid;
    private long eventTime;
    private int bizType;
    private Map<String, Object> fields;
}
