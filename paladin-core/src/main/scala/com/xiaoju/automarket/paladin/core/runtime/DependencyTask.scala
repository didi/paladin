package com.xiaoju.automarket.paladin.core.runtime

import akka.actor.{Actor, ActorRef, Props}
import akka.util.Timeout
import com.xiaoju.automarket.paladin.core.common.Event
import com.xiaoju.automarket.paladin.core.dcg.DependencyDescriptor
import com.xiaoju.automarket.paladin.core.util.ReflectionUtil
import org.slf4j.{Logger, LoggerFactory}

import scala.concurrent.ExecutionContextExecutor
import scala.concurrent.duration._
import scala.language.postfixOps

/**
 * @Author Luogh
 * @Date 2020/12/11
 * */
class DependencyTask(private val dependencyDescriptor: DependencyDescriptor,
                     private val conditionHandler: ConditionHandler
                    ) extends Actor {

  import DependencyTask._

  override def receive: Receive = {
    case event: Event =>
      val curSender: ActorRef = sender()
      implicit val ec: ExecutionContextExecutor = context.system.dispatcher
      implicit val timeout: Timeout = Timeout(120 seconds)
      val result = conditionHandler.doCheck(event)
      curSender ! DependencyResponse(dependencyDescriptor.getDependencyId, result)

    case other@_ =>
      log.warn(s"received unhandled message: $other, ignore it.")
  }
}

object DependencyTask {

  private val log: Logger = LoggerFactory.getLogger(classOf[DependencyTask])

  def apply(env: JobEnvironment, dependencyDescriptor: DependencyDescriptor): Props = {
    val conditionHandler = ReflectionUtil.newInstance(dependencyDescriptor.getConditionDescriptor.getConditionHandler)
    conditionHandler.configure(env.configuration())
    Props(classOf[DependencyTask], dependencyDescriptor, conditionHandler)
  }
}
