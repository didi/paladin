package com.xiaoju.automarket.paladin.core.runtime

import java.util.concurrent.Callable

import akka.actor.{Actor, ActorRef, Props}
import akka.pattern.{Patterns, ask}
import akka.util.Timeout
import com.xiaoju.automarket.paladin.core.common.Event
import com.xiaoju.automarket.paladin.core.dcg.ActionDescriptor
import com.xiaoju.automarket.paladin.core.util.ReflectionUtil
import org.slf4j.{Logger, LoggerFactory}

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContextExecutor, Future}
import scala.language.postfixOps

/**
 * @Author Luogh
 * @Date 2020/12/11
 * */
class TransformActionTask(private val env: JobEnvironment,
                          private val actionHandler: TransformActionHandler,
                          private val actionDescriptor: ActionDescriptor
                         ) extends Actor {

  private var dependencySelector: DependencySelectorLocation = _

  import TransformActionTask._

  override def preStart(): Unit = {
    this.dependencySelector = DependencySelectorLocation(context.actorOf(DependencySelectorTask.apply(env, actionDescriptor)))
  }

  override def receive: Receive = {
    case event: Event =>
      implicit val ec: ExecutionContextExecutor = context.system.dispatcher
      val self = context.self
      this.actionHandler.doAction(event) match {
        case result: ActionResult =>
          if (result.isSuccess) {
            if (result.getFireDuration != null) {
              val callable: Callable[Future[_]] = () => executeDependencySelector(result, self)
              Patterns.after(result.getFireDuration.toMillis millisecond, context.system.scheduler, ec, callable)
            } else {
              executeDependencySelector(result, self)
            }
          }
      }
    case other@_ =>
      log.warn(s"received unhandled message: $other, ignore it.")
  }


  private def executeDependencySelector(actionResult: ActionResult, actor: ActorRef)(implicit ec: ExecutionContextExecutor): Future[DependencySelectorResponse] =
    dependencySelector.actorRef.ask(actionResult.getEvent)(Timeout(120 seconds), actor).mapTo[DependencySelectorResponse]
}

object TransformActionTask {
  private val log: Logger = LoggerFactory.getLogger(classOf[TransformActionTask])

  def apply(env: JobEnvironment, actionDescriptor: ActionDescriptor): Props = {
    val actionHandler = ReflectionUtil.newInstance(actionDescriptor.getActionHandler).asInstanceOf[TransformActionHandler]
    actionHandler.configure(env.configuration())
    Props(classOf[TransformActionTask], env, actionHandler, actionDescriptor)
  }
}
