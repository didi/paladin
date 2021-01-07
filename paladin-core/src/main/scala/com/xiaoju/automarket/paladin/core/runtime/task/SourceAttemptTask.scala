package com.xiaoju.automarket.paladin.core.runtime.task

import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicReference

import akka.actor.{Actor, ActorLogging, ActorRef, Props, Stash}
import akka.pattern.ask
import akka.util.Timeout
import com.google.common.util.concurrent.ThreadFactoryBuilder
import com.xiaoju.automarket.paladin.core.common.ExecutionStateEnum
import com.xiaoju.automarket.paladin.core.runtime.common.Environment
import com.xiaoju.automarket.paladin.core.runtime.executor.JobExecutorInstance
import com.xiaoju.automarket.paladin.core.runtime.message._
import com.xiaoju.automarket.paladin.core.runtime.task.SourceAttemptTask.SOURCE_EXECUTOR_POOL
import com.xiaoju.automarket.paladin.core.runtime.{TaskAttemptId, TaskId}

import scala.collection.mutable.{Map => MMap}
import scala.concurrent.{ExecutionContextExecutor, Future}
import scala.jdk.CollectionConverters.{CollectionHasAsScala, IterableHasAsJava}
import scala.util.{Failure, Success}

/**
 * @Author Luogh
 * @Date 2021/1/4
 * */
class SourceAttemptTask(
                         private val env: Environment,
                         private val jobExecutor: JobExecutorInstance,
                         private val attemptId: TaskAttemptId,
                         private val task: SourceTaskInstance
                       ) extends Actor with Stash with ActorLogging {

  implicit val DEFAULT_TIMEOUT: Timeout = Timeout(env.config.requestTimeout())
  implicit val DEFAULT_EXECUTION_CONTEXT: ExecutionContextExecutor = context.dispatcher

  private val downstreamDependencies: MMap[TaskId, ActorRef] = MMap.empty
  private val currentExecutionState: AtomicReference[ExecutionStateEnum] = new AtomicReference[ExecutionStateEnum](null)

  override def preStart(): Unit = {
    try {
      task.getTaskDescriptor.vertexHandler().initialize(env.config)
      jobExecutor.actorRef ! TaskAttemptExecutionState(task.taskId, attemptId, ExecutionStateEnum.INITIALIZED)
      currentExecutionState.set(ExecutionStateEnum.INITIALIZED)
    } catch {
      case e: Throwable =>
        log.error(s"init source task: $task failed", e)
        jobExecutor.actorRef ! TaskAttemptExecutionFailure(task.taskId, attemptId, e, "init source task failed")
    }
  }


  override def postStop(): Unit = {
    task.getTaskDescriptor.vertexHandler().stop()
  }


  override def receive: Receive = {
    case registry: TaskDependencyRegistry =>
      val dependencyDescriptor = Option(task.getTaskDescriptor.getDownstreamDependencies).map(_.asScala).getOrElse(Seq.empty)
      if (!dependencyDescriptor.exists(dependency => dependency.getDependencyId.equals(registry.taskId))) {
        log.info(s"register a new task downstream dependency that not registered in constructor: $registry")
      }
      downstreamDependencies.put(registry.taskId, registry.taskAttemptActor)

      val allUpstreamMatched = dependencyDescriptor.map(_.getDependencyId).forall(d => downstreamDependencies.contains(d))
      if (allUpstreamMatched) {
        log.info("all upstream & downstream dependencies is registered, switch task execution state to Running.")
        jobExecutor.actorRef ! TaskAttemptExecutionState(task.taskId, attemptId, ExecutionStateEnum.RUNNING)
        currentExecutionState.set(ExecutionStateEnum.RUNNING)
        unstashAll()
        context.become(onRunningState)
        self ! TaskInit
      }

    case _ =>
      stash()
  }


  private def onRunningState: Receive = {
    case TaskInit =>
      SOURCE_EXECUTOR_POOL.submit(new EventGenerator)
  }


  private class EventGenerator extends Runnable {

    override def run(): Unit = {
      val taskHandler = task.getTaskDescriptor.vertexHandler
      while (taskHandler.hashNext() && currentExecutionState.get().equals(ExecutionStateEnum.RUNNING)) {
        val nextEvent = taskHandler.next()
        if (nextEvent != null) {
          val dependencyChecks = for ((_, dependencyAttemptTaskRef) <- downstreamDependencies) yield {
            (dependencyAttemptTaskRef ? nextEvent).mapTo[TaskDependencyMatchResultResponse]
          }

          Future.sequence(dependencyChecks).onComplete {
            case Success(dependencyCheckResponses) =>
              val allMatchedDependencies = dependencyCheckResponses.filter(_.isMatching).map(_.dependencyView)
              val selectDependencies = task.getTaskDescriptor.getDependencySelectorStrategy.select(nextEvent, allMatchedDependencies.asJava)
              selectDependencies.asScala.groupBy(_.getDependencyId).foreach {
                case (dependencyId, dependencies) =>
                  downstreamDependencies(dependencyId) ! TaskDependencyMatchedEvent(nextEvent, dependencies.map(_.getDownstreamVertexId).toSet)
              }
            case Failure(exception) =>
              log.error(s"dependency process failed with exception for event: $nextEvent.", exception)
              jobExecutor.actorRef ! TaskAttemptExecutionFailure(task.taskId, attemptId, exception, s" dependency process event: $nextEvent failed")
          }
        }
      }

      log.info("source task is completed..")
    }
  }

}


object SourceAttemptTask {

  private val SOURCE_EXECUTOR_POOL = Executors.newSingleThreadExecutor(new ThreadFactoryBuilder().setDaemon(false).setNameFormat("EventSource-%d").build())

  def props(env: Environment, jobExecutor: JobExecutorInstance, attemptId: TaskAttemptId, action: SourceTaskInstance): Props =
    Props(new SourceAttemptTask(env, jobExecutor, attemptId: TaskAttemptId, action))
}
