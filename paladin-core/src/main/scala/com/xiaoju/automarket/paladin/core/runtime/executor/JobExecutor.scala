package com.xiaoju.automarket.paladin.core.runtime.executor

import akka.actor.{Actor, ActorLogging, PoisonPill, Props}
import akka.pattern.ask
import akka.util.Timeout
import com.google.common.collect.{HashBasedTable, Table}
import com.xiaoju.automarket.paladin.core.common.ExecutionStateEnum
import com.xiaoju.automarket.paladin.core.dcg.{ActionDescriptor, DependencyDescriptor, JobGraphDescriptor, SourceDescriptor, VertexDescriptor}
import com.xiaoju.automarket.paladin.core.runtime
import com.xiaoju.automarket.paladin.core.runtime._
import com.xiaoju.automarket.paladin.core.runtime.common.Environment
import com.xiaoju.automarket.paladin.core.runtime.message._
import com.xiaoju.automarket.paladin.core.runtime.scheduler.JobScheduler.JobSchedulerInstance
import com.xiaoju.automarket.paladin.core.runtime.task.{ActionAttemptTask, ActionTaskInstance, DependencyAttemptTask, DependencyTaskInstance, SourceAttemptTask, SourceTaskInstance, TaskAttemptInstance, TaskInstance}
import com.xiaoju.automarket.paladin.core.runtime.util.JobGraphTraverseOperator.GraphVisitor
import com.xiaoju.automarket.paladin.core.runtime.util.{JobGraphTraverseOperator, Util}

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
  private val taskAttemptTable: MMap[TaskIdentifier, TaskAttemptInstance] = MMap.empty[TaskIdentifier, TaskAttemptInstance]
  private val jobExecutorRef = JobExecutorInstance(Util.generateUUID, self)

  override def preStart(): Unit = {
    (jobScheduler.actorRef ? JobExecutorRegistration(jobExecutorRef.jobExecutorId, jobId)).onComplete {
      case Success(_) =>
        log.info(s"job executor: ${jobExecutorRef.jobExecutorId} registered to job scheduler ${jobScheduler}  for job: $jobId success. ")
      case Failure(exception: Exception) =>
        log.info(s"job executor: ${jobExecutorRef.jobExecutorId} registered to job scheduler ${jobScheduler}  for job: $jobId failed. ", exception)
        self ! PoisonPill
    }
  }

  override def postStop(): Unit = {
    (jobScheduler.actorRef ? JobExecutorUnRegistration(jobExecutorRef.jobExecutorId, jobId)).onComplete(_ =>
      log.info(s"job executor: $jobExecutorRef.jobExecutorId for job: $jobId stop ..."))
  }

  override def receive: Receive = {
    case JobTaskCreateRequest =>
      submitJobGraph(graphDescriptor)
    case TaskAttemptExecutionState(taskId, taskAttemptId, executionStateEnum, _) =>

    /*taskTable(taskId) match {
      // when the dependency is initialized, notify all the related actions
      case dependency: DependencyTaskInstance if executionStateEnum == ExecutionStateEnum.INITIALIZED =>
        val prevVertexTask = dependency.getTaskDescriptor.getPreVertexDescriptor
        if (prevVertexTask != null) {
          for ((attemptId, _) <- taskExecutionTable.row(prevVertexTask.vertexId()).asScala) {
            taskAttemptTable(attemptId).attemptActorRef ! TaskDependencyRegistry(taskId, taskAttemptId, sender(), downstream = false)
          }
        }
        val nextVertexTask = dependency.getTaskDescriptor.getNextVertexDescriptor
        if (nextVertexTask != null) {
          for ((attemptId, _) <- taskExecutionTable.row(nextVertexTask.vertexId()).asScala) {
            taskAttemptTable(attemptId).attemptActorRef ! TaskDependencyRegistry(taskId, taskAttemptId, sender(), downstream = true)
          }
        }
        // update task execution state
        taskExecutionTable.put(taskId, taskAttemptId, executionStateEnum)
      case action: ActionTaskInstance if executionStateEnum == ExecutionStateEnum.INITIALIZED =>
        // when the action is initialized, notify all the related dependencies
        val downstreamDependencies = action.actionDescriptor.downstreamDependencies()
        if (downstreamDependencies != null) {
          for (dependencyDescriptor <- downstreamDependencies.asScala) {
            for ((attemptId, _) <- taskExecutionTable.row(dependencyDescriptor.getDependencyId).asScala) {
              taskAttemptTable(attemptId).attemptActorRef ! TaskVertexRegistry(taskId, taskAttemptId, sender(), downstream = true)
            }
          }
        }

        val upstreamDependencies = action.actionDescriptor.upstreamDependencies()
        if (upstreamDependencies != null) {
          for (dependencyDescriptor <- upstreamDependencies.asScala) {
            for ((attemptId, _) <- taskExecutionTable.row(dependencyDescriptor.getDependencyId).asScala) {
              taskAttemptTable(attemptId).attemptActorRef ! TaskVertexRegistry(taskId, taskAttemptId, sender(), downstream = false)
            }
          }
        }
        taskExecutionTable.put(taskId, taskAttemptId, executionStateEnum)
      case _ => //
        // update task execution state
        taskExecutionTable.put(taskId, taskAttemptId, executionStateEnum)
    }
*/
    case TaskAttemptExecutionFailure(taskId, taskAttemptId, throwable, msg) =>

      log.info(s"task:$taskId execute failed with attemptId: $taskAttemptId for job $jobId, msg: $msg", throwable)
      // fail all tasks belong to the job
      val stopFutures = for ((_, taskAttemptRef) <- taskAttemptTable) yield {
        taskAttemptRef.attemptActorRef ? PoisonPill
      }

      Future.sequence(stopFutures).onComplete {
        response =>
          log.info(s"stop all task completed with response: $response, stop job: $jobId")
          jobScheduler.actorRef ! JobFailure(jobExecutorRef.jobExecutorId, jobId, throwable, msg)
      }
  }

  private def updateTaskState(taskId: TaskId, taskAttemptId: TaskAttemptId, executionStateEnum: ExecutionStateEnum): Unit = {
    val taskIdTuple = taskId -> taskAttemptId
    if (taskTable.contains(taskId) && taskAttemptTable.contains(taskIdTuple)) {
      // TODO finite-state machine
      val newAttemptTask = taskAttemptTable.get(taskIdTuple).map(prevAttemptTask => prevAttemptTask.copy(executionStateEnum = executionStateEnum))
      taskAttemptTable += taskIdTuple -> newAttemptTask.get


    }
  }


  private def submitJobGraph(jobGraphDescriptor: JobGraphDescriptor): Unit = {
    JobGraphTraverseOperator.traverseInDepth(jobGraphDescriptor, new GraphVisitor {

      override def onVertex(taskId: TaskId, descriptor: VertexDescriptor[_]): Unit = {
        val taskAttemptId = Util.generateUUID
        val (task, actorRef) = descriptor match {
          case action: ActionDescriptor =>
            val task = ActionTaskInstance(jobId, taskId, action)
            val actionActorRef = context.system.actorOf(ActionAttemptTask.props(env, jobExecutorRef, taskAttemptId, task))
            (task, actionActorRef)
          case source: SourceDescriptor =>
            val task = SourceTaskInstance(jobId, taskId, source)
            val sourceActorRef = context.system.actorOf(SourceAttemptTask.props(env, jobExecutorRef, taskAttemptId, task))
            (task, sourceActorRef)
          case other@_ =>
            throw new RuntimeException(s"unknown vertex descriptor: $other")
        }

        taskTable += taskId -> task
        val attemptTask = runtime.task.TaskAttemptInstance(taskId, taskAttemptId, actorRef, ExecutionStateEnum.DEPLOYED)
        taskAttemptTable += (taskId -> attemptTask.attemptId) -> attemptTask
      }

      override def onEdge(taskId: TaskId, descriptor: DependencyDescriptor): Unit = {
        val task = DependencyTaskInstance(jobId, taskId, descriptor)
        val taskAttemptId = Util.generateUUID
        val actionActorRef = context.system.actorOf(DependencyAttemptTask.props(env, jobExecutorRef, taskAttemptId, task))
        val attemptTask = runtime.task.TaskAttemptInstance(taskId, taskAttemptId, actionActorRef, ExecutionStateEnum.DEPLOYED)
        taskAttemptTable += (taskId -> attemptTask.attemptId) -> attemptTask
        taskTable += taskId -> task
      }
    })
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
