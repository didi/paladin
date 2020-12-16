package com.xiaoju.automarket.paladin.core.runtime.message;

public class TaskInitStateRequest implements TaskStateRequest {

    private static final TaskInitStateRequest INSTANCE = new TaskInitStateRequest();

    private TaskInitStateRequest() {
    }

    public static TaskInitStateRequest getInstance() {
        return INSTANCE;
    }
}