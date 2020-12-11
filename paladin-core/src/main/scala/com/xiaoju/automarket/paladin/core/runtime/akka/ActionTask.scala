package com.xiaoju.automarket.paladin.core.runtime.akka

import java.util.concurrent.Callable

import akka.actor.{Actor, ActorRef, Props, SupervisorStrategy}
import akka.pattern.Patterns
import com.xiaoju.automarket.paladin.core.common.Event
import com.xiaoju.automarket.paladin.core.dcg.ActionDescriptor
import com.xiaoju.automarket.paladin.core.runtime.{ActionHandler, ActionResult, JobEnvironment}
import com.xiaoju.automarket.paladin.core.util.ReflectionUtil
import org.slf4j.{Logger, LoggerFactory}

import scala.concurrent.duration.Duration
import scala.concurrent.{ExecutionContextExecutor, Future}

/**
 * @Author Luogh
 * @Date 2020/12/11
 * */
class ActionTask(private val actionHandler: ActionHandler, private val actionDescriptor: ActionDescriptor) extends Actor {

  private var dependencySelectorActorRef: ActorRef = _

  import ActionTask._

  override def supervisorStrategy: SupervisorStrategy = super.supervisorStrategy

  override def preStart(): Unit = {
    this.dependencySelectorActorRef = context.actorOf(DependencySelectorTask.apply(actionDescriptor))
  }

  override def receive: Receive = {
    case event: Event =>
      implicit val ec: ExecutionContextExecutor = context.system.dispatcher
      this.actionHandler.doAction(event) match {
        case result: ActionResult =>
          if (result.isSuccess) {
            if (result.getFireDuration != null) {
              val callable: Callable[Future[_]] = () => {
                Future {
                  dependencySelectorActorRef ! result.getEvent
                }
              }
              Patterns.after(Duration.create(result.getFireDuration.toMillis, "millis"), context.system.scheduler, ec, callable)
            } else {
              dependencySelectorActorRef ! result.getEvent
            }
          }
      }

    case DependencySelectorResult(dependencies, event) =>
      log.info(s"after dependency selector applying, current select ${dependencies.size} dependencies")
      for (dependency <- dependencies) {
        dependency.actorRef ! event
      }

    case other@_ =>
      log.warn(s"received unhandled message: $other, ignore it.")
  }

  override def postStop(): Unit = {
    if (actionHandler != null) {
      actionHandler.destroy()
    }
  }
}


object ActionTask {
  private val log: Logger = LoggerFactory.getLogger(classOf[ActionTask])

  def apply(env: JobEnvironment, actionDescriptor: ActionDescriptor): Props = {
    val actionHandler = ReflectionUtil.newInstance(actionDescriptor.getActionHandler)
    actionHandler.configure(env.configuration())
    Props(classOf[ActionTask], actionHandler, actionDescriptor)
  }
}
