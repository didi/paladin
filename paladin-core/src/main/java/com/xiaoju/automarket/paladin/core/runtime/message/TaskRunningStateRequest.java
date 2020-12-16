package com.xiaoju.automarket.paladin.core.runtime.message;

public class TaskRunningStateRequest implements TaskStateRequest {

    private static final TaskRunningStateRequest INSTANCE = new TaskRunningStateRequest();

    private TaskRunningStateRequest() {
    }

    public static TaskRunningStateRequest getInstance() {
        return INSTANCE;
    }
}