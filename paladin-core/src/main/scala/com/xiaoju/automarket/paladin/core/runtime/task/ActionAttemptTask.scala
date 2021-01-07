package com.xiaoju.automarket.paladin.core.runtime.task

import java.time.Duration
import java.util.concurrent.TimeUnit

import akka.actor.{Actor, ActorLogging, ActorRef, Props, Stash}
import akka.pattern.ask
import akka.util.Timeout
import com.xiaoju.automarket.paladin.core.common.ExecutionStateEnum
import com.xiaoju.automarket.paladin.core.dcg.{ActionHandler, BizCallbackEvent, BizEvent}
import com.xiaoju.automarket.paladin.core.runtime._
import com.xiaoju.automarket.paladin.core.runtime.common.Environment
import com.xiaoju.automarket.paladin.core.runtime.executor.JobExecutorInstance
import com.xiaoju.automarket.paladin.core.runtime.message._
import com.xiaoju.automarket.paladin.core.runtime.task.ActionAttemptTask.ActionBizEventResult
import net.jodah.expiringmap.{ExpirationListener, ExpirationPolicy, ExpiringMap}

import scala.collection.mutable.{Map => MMap}
import scala.concurrent.duration.FiniteDuration
import scala.concurrent.{ExecutionContextExecutor, Future}
import scala.jdk.CollectionConverters.{CollectionHasAsScala, IterableHasAsJava}
import scala.util.{Failure, Success}

/**
 * @Author Luogh
 * @Date 2020/12/17
 * */
class ActionAttemptTask(
                         private val env: Environment,
                         private val jobExecutor: JobExecutorInstance,
                         private val attemptId: TaskAttemptId,
                         private val task: ActionTaskInstance
                       ) extends Actor with Stash with ActorLogging {

  implicit val DEFAULT_TIMEOUT: Timeout = Timeout(env.config.requestTimeout())
  implicit val DEFAULT_EXECUTION_CONTEXT: ExecutionContextExecutor = context.dispatcher

  private val downstreamDependencies: MMap[TaskId, ActorRef] = MMap.empty
  private val upstreamDependencies: MMap[TaskId, ActorRef] = MMap.empty
  private var actionEventCollector: ActionEventCollector = _

  override def preStart(): Unit = {
    try {
      task.actionDescriptor.vertexHandler().initialize(env.config)
      actionEventCollector = ActionEventCollector(self)
      jobExecutor.actorRef ! TaskAttemptExecutionState(task.taskId, attemptId, ExecutionStateEnum.INITIALIZED)
    } catch {
      case e: Throwable =>
        log.error(s"init action task: $task failed", e)
        jobExecutor.actorRef ! TaskAttemptExecutionFailure(task.taskId, attemptId, e, "init action task failed")
    }
  }

  override def receive: Receive = {
    case registry: TaskDependencyRegistry =>
      registerTaskDependency(registry)
      // when all the dependency defined in the constructor registered, set action task state to running
      val allDownstreamMatched = Option(task.getTaskDescriptor.downstreamDependencies())
        .map(_.asScala)
        .getOrElse(Seq.empty)
        .map(_.getDependencyId)
        .forall(d => downstreamDependencies.contains(d))

      val allUpstreamMatched = Option(task.getTaskDescriptor.upstreamDependencies())
        .map(_.asScala)
        .getOrElse(Seq.empty)
        .map(_.getDependencyId).forall(d => upstreamDependencies.contains(d))

      if (allDownstreamMatched && allUpstreamMatched) {
        log.info("all upstream & downstream dependencies is registered, switch task execution state to Running.")
        jobExecutor.actorRef ! TaskAttemptExecutionState(task.taskId, attemptId, ExecutionStateEnum.RUNNING)
        unstashAll()
        context.become(onRunningState)
      }

    case _ =>
      stash()
  }


  override def postStop(): Unit = {
    super.postStop()
    log.info(s"action attempt task: ${task.taskId} job: ${task.jobId} stop ...")
    task.actionDescriptor.vertexHandler().stop()
  }

  private def onRunningState: Receive = {
    case bizEvent: BizEvent =>
      try {
        val actionHandler = task.getTaskDescriptor.vertexHandler
        actionHandler.doAction(bizEvent, actionEventCollector)
      } catch {
        case e: Throwable =>
          log.error(s"action process event: $bizEvent failed", e)
          jobExecutor.actorRef ! TaskAttemptExecutionFailure(task.taskId, attemptId, e, s" action process event: $bizEvent failed")
      }
    case bizCallbackEvent: BizCallbackEvent =>
      try {
        if (ActionEventCollector.CALLBACK_HANDLERS.containsKey(bizCallbackEvent.getCallbackId)) {
          val callback = ActionEventCollector.CALLBACK_HANDLERS.get(bizCallbackEvent.getCallbackId)
          ActionEventCollector.CALLBACK_HANDLERS.remove(bizCallbackEvent.getCallbackId)
          callback.handle(bizCallbackEvent)
        }
      } catch {
        case e: Throwable =>
          log.error(s"action process callback event: $bizCallbackEvent failed", e)
          jobExecutor.actorRef ! TaskAttemptExecutionFailure(task.taskId, attemptId, e, s" action process callback event: $bizCallbackEvent failed")
      }
    case ActionBizEventResult(bizEvent, duration) =>
      if (duration.isEmpty) {
        dependencyCheck(bizEvent)
      } else {
        context.system.getScheduler.scheduleOnce(duration.get, new Runnable {
          override def run(): Unit = dependencyCheck(bizEvent)
        })
      }
    case registry: TaskDependencyRegistry =>
      registerTaskDependency(registry)
  }


  private def dependencyCheck(bizEvent: BizEvent): Unit = {
    val dependencyChecks = for ((_, dependencyAttemptTaskRef) <- downstreamDependencies) yield {
      (dependencyAttemptTaskRef ? bizEvent).mapTo[TaskDependencyMatchResultResponse]
    }
    Future.sequence(dependencyChecks).onComplete {
      case Success(dependencyCheckResponses) =>
        val allMatchedDependencies = dependencyCheckResponses.filter(_.isMatching).map(_.dependencyView)
        val selectDependencies = task.actionDescriptor.downstreamDependencySelectorStrategy().select(bizEvent, allMatchedDependencies.asJava)
        selectDependencies.asScala.groupBy(_.getDependencyId).foreach {
          case (dependencyId, dependencies) =>
            downstreamDependencies(dependencyId) ! TaskDependencyMatchedEvent(bizEvent, dependencies.map(_.getDownstreamVertexId).toSet)
        }
      case Failure(exception) =>
        log.error(s"dependency process failed with exception for event: $bizEvent.", exception)
        jobExecutor.actorRef ! TaskAttemptExecutionFailure(task.taskId, attemptId, exception, s" dependency process event: $bizEvent failed")
    }
  }

  private def registerTaskDependency(registry: TaskDependencyRegistry): Unit = {
    if (registry.downstream) {
      val dependencyDescriptor = task.getTaskDescriptor.downstreamDependencies().asScala
        .find(dependency => dependency.getDependencyId.equals(registry.taskId))
      if (dependencyDescriptor.isEmpty) {
        log.info(s"register a new task downstream dependency that not registered in constructor: $registry")
      }
      downstreamDependencies.put(registry.taskId, registry.taskAttemptActor)
    } else {
      val dependencyDescriptor = task.getTaskDescriptor.upstreamDependencies().asScala
        .find(dependency => dependency.getDependencyId.equals(registry.taskId))
      if (dependencyDescriptor.isEmpty) {
        log.info(s"register a new task upstream dependency that not registered in constructor: $registry")
      }
      upstreamDependencies.put(registry.taskId, registry.taskAttemptActor)
    }
  }


  class ActionEventCollector(val actorRef: ActorRef) extends ActionHandler.Collector {

    import ActionEventCollector._

    override def collect(out: BizEvent, fireDuration: Duration): Unit = {
      require(out != null && fireDuration != null, "event & fire duration cant be null")
      actorRef ! ActionBizEventResult(out, Some(FiniteDuration(fireDuration.toMillis, TimeUnit.MILLISECONDS)))
    }

    override def collect(out: BizEvent): Unit = actorRef ! ActionBizEventResult(out)

    override def onCallback(callbackId: String, callbackHandler: ActionHandler.ActionCallbackHandler, timeout: Duration): Unit = {
      CALLBACK_HANDLERS.put(callbackId, callbackHandler, timeout.toMillis, TimeUnit.MILLISECONDS)

      CALLBACK_HANDLERS.addAsyncExpirationListener(new ExpirationListener[String, ActionHandler.ActionCallbackHandler]() {
        override def expired(key: String, callbackHandler: ActionHandler.ActionCallbackHandler): Unit = {
          callbackHandler.onTimeout()
        }
      })
    }


  }

  object ActionEventCollector {
    def apply(actorRef: ActorRef) = new ActionEventCollector(actorRef)

    /**
     * all registered callback handlers
     */
    val CALLBACK_HANDLERS: ExpiringMap[String, ActionHandler.ActionCallbackHandler] = ExpiringMap.builder()
      .variableExpiration()
      .expirationPolicy(ExpirationPolicy.CREATED)
      .build()
  }

}

object ActionAttemptTask {

  def props(env: Environment, jobExecutor: JobExecutorInstance, attemptId: TaskAttemptId, action: ActionTaskInstance): Props =
    Props(new ActionAttemptTask(env, jobExecutor, attemptId: TaskAttemptId, action))

  case class ActionBizEventResult(out: BizEvent, fireDuration: Option[FiniteDuration] = None)

}
