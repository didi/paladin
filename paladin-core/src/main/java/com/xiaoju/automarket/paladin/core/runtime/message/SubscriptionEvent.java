package com.xiaoju.automarket.paladin.core.runtime.message;

import java.util.Map;

public interface SubscriptionEvent {
    String sourceType();

    String eventId();

    String eventType();

    long eventTime();

    Map<String, Object> fields();
}