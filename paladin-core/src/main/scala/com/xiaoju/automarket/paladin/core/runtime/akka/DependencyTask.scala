package com.xiaoju.automarket.paladin.core.runtime.akka

import akka.actor.{Actor, Props}
import org.slf4j.{Logger, LoggerFactory}

/**
 * @Author Luogh
 * @Date 2020/12/11
 * */
class DependencyTask extends Actor {
  override def receive: Receive = ???
}

object DependencyTask {

  private val log: Logger = LoggerFactory.getLogger(classOf[DependencyTask])


  def apply(): Props = {

  }
}
