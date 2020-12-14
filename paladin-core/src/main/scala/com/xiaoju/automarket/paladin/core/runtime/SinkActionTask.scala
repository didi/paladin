package com.xiaoju.automarket.paladin.core.runtime

import akka.actor.{Actor, Props}
import com.xiaoju.automarket.paladin.core.common.Event
import com.xiaoju.automarket.paladin.core.dcg.ActionDescriptor
import com.xiaoju.automarket.paladin.core.util.ReflectionUtil
import org.slf4j.{Logger, LoggerFactory}

/**
 * @Author Luogh
 * @Date 2020/12/14
 * */
class SinkActionTask(private val env: JobEnvironment,
                     private val actionDescriptor: ActionDescriptor,
                     private val sinkActionHandler: SinkActionHandler
                    ) extends Actor {

  import SinkActionTask._

  override def receive: Receive = {
    case event: Event =>
      sinkActionHandler.doAction(event)
    case other@_ =>
      log.warn(s"received unhandled message: $other, ignore it.")
  }
}

object SinkActionTask {

  private val log: Logger = LoggerFactory.getLogger(classOf[SinkActionTask])

  def apply(env: JobEnvironment, action: ActionDescriptor): Props = {
    val sinkActionHandler = ReflectionUtil.newInstance(action.getActionHandler).asInstanceOf[SinkActionHandler]
    Props(classOf[SinkActionTask], env, action, sinkActionHandler)
  }
}
