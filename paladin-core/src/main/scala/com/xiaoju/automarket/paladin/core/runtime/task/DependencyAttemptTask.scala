package com.xiaoju.automarket.paladin.core.runtime.task

import akka.actor.{Actor, ActorLogging, ActorRef, Props, Stash}
import akka.util.Timeout
import com.xiaoju.automarket.paladin.core.common.ExecutionStateEnum
import com.xiaoju.automarket.paladin.core.dcg.BizEvent
import com.xiaoju.automarket.paladin.core.runtime._
import com.xiaoju.automarket.paladin.core.runtime.common.Environment
import com.xiaoju.automarket.paladin.core.runtime.executor.JobExecutorInstance
import com.xiaoju.automarket.paladin.core.runtime.message._

import scala.collection.mutable.{Map => MMap}
import scala.concurrent.ExecutionContextExecutor

/**
 * @Author Luogh
 * @Date 2020/12/17
 * */
class DependencyAttemptTask(private val env: Environment,
                            private val jobExecutor: JobExecutorInstance,
                            private val taskAttemptId: TaskAttemptId,
                            private val task: DependencyTaskInstance) extends Actor with Stash with ActorLogging {

  implicit val DEFAULT_TIMEOUT: Timeout = Timeout(env.config.requestTimeout())
  implicit val DEFAULT_EXECUTION_CONTEXT: ExecutionContextExecutor = context.dispatcher

  private val downstreamActions: MMap[TaskId, ActorRef] = MMap.empty
  private val upstreamActions: MMap[TaskId, ActorRef] = MMap.empty

  override def preStart(): Unit = {
    try {
      task.dependencyDescriptor.getConditionHandler.initialize(env.config)
      jobExecutor.actorRef ! TaskAttemptExecutionState(task.taskId, taskAttemptId, ExecutionStateEnum.INITIALIZED)
    } catch {
      case e: Throwable =>
        log.error(s"init dependency task: $task failed", e)
        jobExecutor.actorRef ! TaskAttemptExecutionFailure(task.taskId, taskAttemptId, e, "init dependency task failed")
    }
  }


  override def postStop(): Unit = {
    task.dependencyDescriptor.getConditionHandler.stop()
  }

  override def receive: Receive = {
    case event@TaskActionRegistry(actionId, _, actionAttemptTaskRef: ActorRef, downstream) =>
      if (downstream) {
        val expectedActionId = task.dependencyDescriptor.getNextActionDescriptor.getActionId
        require(actionId == expectedActionId, s"received an unexpected downstream action registry event: $event")
        downstreamActions += actionId -> actionAttemptTaskRef
      } else {
        val expectedActionId = task.dependencyDescriptor.getPreActionDescriptor.getActionId
        require(actionId == expectedActionId, s"received an unexpected upstream action registry event: $event")
        upstreamActions += actionId -> actionAttemptTaskRef
      }
      // when all action registry is completed, set dependency as running state
      val upstreamActionCompleted =
        if (task.dependencyDescriptor.getPreActionDescriptor != null)
          upstreamActions.contains(task.dependencyDescriptor.getPreActionDescriptor.getActionId) else false
      val downstreamActionCompleted =
        if (task.dependencyDescriptor.getNextActionDescriptor != null)
          downstreamActions.contains(task.dependencyDescriptor.getNextActionDescriptor.getActionId) else false

      if (upstreamActionCompleted && downstreamActionCompleted) {
        jobExecutor.actorRef ! TaskAttemptExecutionState(task.taskId, taskAttemptId, ExecutionStateEnum.RUNNING)
      }
      unstashAll()
      context.become(onRunningState)
    case _: BizEvent =>
      stash()
    case _: TaskDependencyMatchedEvent =>
      stash()
  }

  private def onRunningState: Receive = {
    case bizEvent: BizEvent =>
      val isMatched = task.dependencyDescriptor.getConditionHandler.test(bizEvent)
      sender() ! TaskDependencyMatchResultResponse(task.taskId, isMatched, task.dependencyDescriptor.toView)

    case TaskDependencyMatchedEvent(event, nextActions) =>
      downstreamActions.filter { case (taskId, _) =>
        nextActions.contains(taskId)
      }.foreach {
        case (_, actorRef) =>
          actorRef forward event
      }

  }
}

object DependencyAttemptTask {
  def props(env: Environment, jobExecutor: JobExecutorInstance, taskAttemptId: TaskAttemptId, task: DependencyTaskInstance): Props = {
    Props(new DependencyAttemptTask(env, jobExecutor, taskAttemptId, task))
  }
}