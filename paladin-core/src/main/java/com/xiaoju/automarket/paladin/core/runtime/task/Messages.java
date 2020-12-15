package com.xiaoju.automarket.paladin.core.runtime.task;

import akka.actor.ActorRef;
import com.xiaoju.automarket.paladin.core.dcg.DependencyDescriptor.DependencyDescriptorView;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

/**
 * @Author Luogh
 * @Date 2020/12/15
 **/
public class Messages {

    public interface SubscriptionEvent {
        String sourceType();

        String eventId();

        String eventType();

        long eventTime();

        Map<String, Object> fields();
    }

    @Getter
    @AllArgsConstructor
    public static class RegisterDependency {
        private final boolean isOutside;
        private final int dependencyId;
        private final ActorRef taskRef;
        private final DependencyDescriptorView dependency;
    }

    @Getter
    @AllArgsConstructor
    public static class RegisterAction {
        private final int actionId;
        private final ActorRef taskRef;
    }

    @Getter
    @AllArgsConstructor
    public static class UnRegisterDependencyRequest {
        private final boolean isOutside;
        private final int dependencyId;
    }


    public interface TaskStateRequest {
    }

    public static class TaskInitStateRequest implements TaskStateRequest {
    }

    public static class TaskRunningStateRequest implements TaskStateRequest {
    }

    public static class TaskCancelStateRequest implements TaskStateRequest {
    }

    @Getter
    @Setter
    public static class TaskDependencyCheckRequest {
        private SubscriptionEvent event;
    }

    @Getter
    @Setter
    public static class TaskDependencyRouterRequest {
        private SubscriptionEvent event;
        private int actionId;
    }

    @Getter
    @Setter
    public static class TaskDependencyResponse {
        private int dependencyId;
        private boolean isMatched;
        private Throwable exception;
    }

    @Getter
    @Setter
    public static class Response<T> {
        private boolean isSuccess;
        private T data;
        private Throwable exception;
    }

}
