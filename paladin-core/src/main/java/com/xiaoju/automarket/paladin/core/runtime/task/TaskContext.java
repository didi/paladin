package com.xiaoju.automarket.paladin.core.runtime.task;

import akka.actor.ActorSystem;
import com.typesafe.config.Config;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @Author Luogh
 * @Date 2020/12/15
 **/
@Getter
@AllArgsConstructor
public class TaskContext {
    private final ActorSystem actorSystem;
    private final Config config;
    private final String taskId;
    private final String taskName;

    public String getTaskInstanceId() {
        return this.taskName + "_" + this.taskId;
    }
}
