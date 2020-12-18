package com.xiaoju.automarket.paladin.core.runtime.message

/**
 * @Author Luogh
 * @Date 2020/12/17
 * */

import akka.actor.ActorRef
import com.xiaoju.automarket.paladin.core.common.ExecutionStateEnum
import com.xiaoju.automarket.paladin.core.dcg.{ActionDescriptor, BizEvent, DependencyDescriptor}
import com.xiaoju.automarket.paladin.core.dcg.DependencyDescriptor.DependencyDescriptorView
import com.xiaoju.automarket.paladin.core.runtime.{JobExecutorId, JobId, TaskAttemptId, TaskId}

/**
 * @Author Luogh
 * @Date 2020/12/17
 * */
case class TaskCancelRequest(jobId: JobId, taskId: TaskId)

case class TaskCancelResponse(taskExecutorId: JobExecutorId, jobID: JobId, taskId: TaskId)

case class ActionTaskCreateRequest(jobId: JobId, taskId: TaskId, taskDescriptor: ActionDescriptor)

case class DependencyTaskCreateRequest(jobId: JobId, taskId: TaskId, dependencyDescriptor: DependencyDescriptor)

case class TaskCreateResponse(jobId: JobId, taskId: TaskId, taskAttemptID: TaskAttemptId, success: Boolean, throwable: Option[Throwable] = None)

case class TaskAttemptExecutionState(taskId: TaskId, taskAttemptId: TaskAttemptId, executionStateEnum: ExecutionStateEnum, msg: String = "")

case class TaskAttemptExecutionFailure(taskId: TaskId, taskAttemptId: TaskAttemptId, throwable: Throwable, msg: String = "")

case class TaskAttemptExecutionInitialized(taskId: TaskId, taskAttemptId: TaskAttemptId, throwable: Throwable, msg: String = "")

case class TaskDependencyRegistry(taskId: TaskId, taskAttemptId: TaskAttemptId, taskAttemptActor: ActorRef, downstream: Boolean /*is downstream or upstream */)

case class TaskDependencyMatchResultResponse(taskId: TaskId, isMatching: Boolean, dependencyView: DependencyDescriptorView)

case class TaskDependencyMatchedEvent(event: BizEvent, downstreamActionTasks: Set[TaskId])

case class TaskActionRegistry(taskId: TaskId, taskAttemptId: TaskAttemptId, taskAttemptActor: ActorRef, downstream: Boolean)