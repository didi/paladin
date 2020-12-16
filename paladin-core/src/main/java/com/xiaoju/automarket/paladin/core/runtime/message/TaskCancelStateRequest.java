package com.xiaoju.automarket.paladin.core.runtime.message;

public class TaskCancelStateRequest implements TaskStateRequest {

    private static final TaskCancelStateRequest INSTANCE = new TaskCancelStateRequest();

    private TaskCancelStateRequest() {
    }

    public static TaskCancelStateRequest getInstance() {
        return INSTANCE;
    }

}