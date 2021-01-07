package com.xiaoju.automarket.paladin.core.runtime.task

import akka.actor.ActorRef
import com.xiaoju.automarket.paladin.core.common.ExecutionStateEnum
import com.xiaoju.automarket.paladin.core.runtime.{TaskAttemptId, TaskId}

/**
 * @Author Luogh
 * @Date 2020/12/17
 * */

case class TaskAttemptInstance(taskId: TaskId, attemptId: TaskAttemptId,
                               attemptActorRef: ActorRef,
                               executionStateEnum: ExecutionStateEnum = ExecutionStateEnum.DEPLOYING)