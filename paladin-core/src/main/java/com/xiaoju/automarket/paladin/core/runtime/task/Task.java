package com.xiaoju.automarket.paladin.core.runtime.task;

/**
 * @Author Luogh
 * @Date 2020/12/16
 **/
public interface Task {
    void initialize(TaskContext context);

    void run();

    void stop();
}
