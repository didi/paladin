package com.xiaoju.automarket.paladin.core.runtime.executor

import akka.actor.{Actor, ActorLogging, PoisonPill, Props}
import akka.pattern.ask
import akka.util.Timeout
import com.google.common.collect.{HashBasedTable, Table}
import com.xiaoju.automarket.paladin.core.common.ExecutionStateEnum
import com.xiaoju.automarket.paladin.core.dcg.{ActionDescriptor, DependencyDescriptor, JobGraphDescriptor}
import com.xiaoju.automarket.paladin.core.runtime
import com.xiaoju.automarket.paladin.core.runtime._
import com.xiaoju.automarket.paladin.core.runtime.common.Environment
import com.xiaoju.automarket.paladin.core.runtime.message._
import com.xiaoju.automarket.paladin.core.runtime.scheduler.JobSchedulerInstance
import com.xiaoju.automarket.paladin.core.runtime.task.{ActionAttemptTask, ActionTaskInstance, DependencyAttemptTask, DependencyTaskInstance, TaskAttemptInstance, TaskInstance}
import com.xiaoju.automarket.paladin.core.runtime.util.Util

import scala.collection.mutable.{Map => MMap}
import scala.concurrent.{ExecutionContextExecutor, Future}
import scala.jdk.CollectionConverters.{CollectionHasAsScala, MapHasAsScala}
import scala.util.{Failure, Success}


/**
 * @Author Luogh
 * @Date 2020/12/17
 * */
class JobExecutor(val env: Environment,
                  val jobId: JobId,
                  val graphDescriptor: JobGraphDescriptor,
                  val jobScheduler: JobSchedulerInstance
                 ) extends Actor with ActorLogging {

  implicit val DEFAULT_TIMEOUT: Timeout = Timeout(env.config.requestTimeout())
  implicit val DEFAULT_EXECUTION_CONTEXT: ExecutionContextExecutor = context.dispatcher

  private val taskTable: MMap[TaskId, TaskInstance] = MMap.empty[TaskId, TaskInstance]
  private val taskAttemptTable: MMap[TaskAttemptId, TaskAttemptInstance] = MMap.empty[TaskAttemptId, TaskAttemptInstance]
  private val taskExecutionTable: Table[TaskId, TaskAttemptId, ExecutionStateEnum] = HashBasedTable.create()

  private val jobExecutorId: JobExecutorId = Util.generateUUID


  override def preStart(): Unit = {
    (jobScheduler.actorRef ? JobExecutorRegistration(jobExecutorId, jobId)).onComplete {
      case Success(_) =>
        log.info(s"job executor: $jobExecutorId registered to job scheduler ${jobScheduler}  for job: $jobId success. ")
      case Failure(exception: Exception) =>
        log.info(s"job executor: $jobExecutorId registered to job scheduler ${jobScheduler}  for job: $jobId failed. ", exception)
        self ! PoisonPill
    }
  }

  override def postStop(): Unit = {
    (jobScheduler.actorRef ? JobExecutorUnRegistration(jobExecutorId, jobId)).onComplete(_ =>
      log.info(s"job executor: $jobExecutorId for job: $jobId stop ..."))
  }

  override def receive: Receive = {
    case JobTaskCreateRequest =>

      val jobExecutorRef = JobExecutorInstance(jobExecutorId, self)
      val actions: MMap[TaskId, ActionDescriptor] = graphDescriptor.getActions.asScala
      for ((taskId, actionDescriptor) <- actions) {
        val task = ActionTaskInstance(jobId, taskId, actionDescriptor)
        val taskAttemptId = Util.generateUUID
        val actionActorRef = context.system.actorOf(ActionAttemptTask.props(env, jobExecutorRef, taskAttemptId, task))
        val attemptTask = runtime.task.TaskAttemptInstance(taskId, taskAttemptId, actionActorRef)
        taskAttemptTable += attemptTask.attemptId -> attemptTask
        taskTable += taskId -> task
        taskExecutionTable.put(taskId, attemptTask.attemptId, ExecutionStateEnum.DEPLOYED)
      }
      val dependencies: MMap[TaskId, DependencyDescriptor] = graphDescriptor.getDependencies.asScala
      for ((taskId, dependencyDescriptor) <- dependencies) {
        val task = DependencyTaskInstance(jobId, taskId, dependencyDescriptor)
        val taskAttemptId = Util.generateUUID
        val actionActorRef = context.system.actorOf(DependencyAttemptTask.props(env, jobExecutorRef, taskAttemptId, task))
        val attemptTask = runtime.task.TaskAttemptInstance(taskId, taskAttemptId, actionActorRef)
        taskAttemptTable += attemptTask.attemptId -> attemptTask
        taskTable += taskId -> task
        taskExecutionTable.put(taskId, attemptTask.attemptId, ExecutionStateEnum.DEPLOYED)
      }
      log.info(s"job $jobId start initial all the tasks: $taskTable")

    case TaskAttemptExecutionState(taskId, taskAttemptId, executionStateEnum, _) =>

      taskTable(taskId) match {
        // when the dependency is initialized, notify all the related actions
        case dependency: DependencyTaskInstance if executionStateEnum == ExecutionStateEnum.INITIALIZED =>
          val prevActionTask = dependency.getTaskDescriptor.getPreActionDescriptor
          if (prevActionTask != null) {
            for ((attemptId, _) <- taskExecutionTable.row(prevActionTask.getActionId).asScala) {
              taskAttemptTable(attemptId).attemptActorRef ! TaskDependencyRegistry(taskId, taskAttemptId, sender(), downstream = false)
            }
          }
          val nextActionTask = dependency.getTaskDescriptor.getNextActionDescriptor
          if (nextActionTask != null) {
            for ((attemptId, _) <- taskExecutionTable.row(nextActionTask.getActionId).asScala) {
              taskAttemptTable(attemptId).attemptActorRef ! TaskDependencyRegistry(taskId, taskAttemptId, sender(), downstream = true)
            }
          }
          // update task execution state
          taskExecutionTable.put(taskId, taskAttemptId, executionStateEnum)
        case action: ActionTaskInstance if executionStateEnum == ExecutionStateEnum.INITIALIZED =>
          // when the action is initialized, notify all the related dependencies
          val downstreamDependencies = action.actionDescriptor.getDownstreamDependencies
          if (downstreamDependencies != null) {
            for (dependencyDescriptor <- downstreamDependencies.asScala) {
              for ((attemptId, _) <- taskExecutionTable.row(dependencyDescriptor.getDependencyId).asScala) {
                taskAttemptTable(attemptId).attemptActorRef ! TaskActionRegistry(taskId, taskAttemptId, sender(), downstream = true)
              }
            }
          }

          val upstreamDependencies = action.actionDescriptor.getUpstreamDependencies
          if (upstreamDependencies != null) {
            for (dependencyDescriptor <- upstreamDependencies.asScala) {
              for ((attemptId, _) <- taskExecutionTable.row(dependencyDescriptor.getDependencyId).asScala) {
                taskAttemptTable(attemptId).attemptActorRef ! TaskActionRegistry(taskId, taskAttemptId, sender(), downstream = false)
              }
            }
          }
          taskExecutionTable.put(taskId, taskAttemptId, executionStateEnum)
        case _ => //
          // update task execution state
          taskExecutionTable.put(taskId, taskAttemptId, executionStateEnum)
      }

    case TaskAttemptExecutionFailure(taskId, taskAttemptId, throwable, msg) =>

      log.info(s"task:$taskId execute failed with attemptId: $taskAttemptId for job $jobId, msg: $msg", throwable)
      // fail all tasks belong to the job

      val stopFutures = for ((_, taskAttemptRef) <- taskAttemptTable) yield {
        taskAttemptRef.attemptActorRef ? PoisonPill
      }

      Future.sequence(stopFutures).onComplete {
        response =>
          log.info(s"stop all task completed with response: $response, stop job: $jobId")
          jobScheduler.actorRef ! JobFailure(jobExecutorId, jobId, throwable, msg)
      }
  }
}

object JobExecutor {

  def props(env: Environment,
            jobId: JobId,
            graphDescriptor: JobGraphDescriptor,
            jobScheduler: JobSchedulerInstance
           ): Props = {
    require(graphDescriptor != null && graphDescriptor.getActions != null && !graphDescriptor.getActions.isEmpty, "job graph must have at least one action.")
    Props(new JobExecutor(env, jobId, graphDescriptor, jobScheduler))
  }
}
