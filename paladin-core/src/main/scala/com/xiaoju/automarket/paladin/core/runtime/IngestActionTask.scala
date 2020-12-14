package com.xiaoju.automarket.paladin.core.runtime

import akka.actor.{Actor, Props}
import com.xiaoju.automarket.paladin.core.dcg.ActionDescriptor
import com.xiaoju.automarket.paladin.core.util.ReflectionUtil
import org.slf4j.{Logger, LoggerFactory}

/**
 * @Author Luogh
 * @Date 2020/12/14
 * */
class IngestActionTask(
                        private val environment: JobEnvironment,
                        private val dependencyDescriptor: ActionDescriptor,
                        private val ingestActionHandler: IngestActionHandler
                      ) extends Actor {
  override def receive: Receive = ???
}

object IngestActionTask {

  private val log: Logger = LoggerFactory.getLogger(classOf[IngestActionTask])

  def apply(env: JobEnvironment, actionDescriptor: ActionDescriptor): Props = {
    val actionHandler = ReflectionUtil.newInstance(actionDescriptor.getActionHandler).asInstanceOf[IngestActionHandler]
    actionHandler.configure(env.configuration())
    Props(classOf[IngestActionTask], env, actionDescriptor, actionHandler)
  }
}
