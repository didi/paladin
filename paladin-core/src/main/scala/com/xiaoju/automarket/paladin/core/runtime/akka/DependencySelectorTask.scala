package com.xiaoju.automarket.paladin.core.runtime.akka

import akka.actor.{Actor, Props}
import com.xiaoju.automarket.paladin.core.common.Event
import com.xiaoju.automarket.paladin.core.dcg.{ActionDescriptor, DependencyDescriptor}
import com.xiaoju.automarket.paladin.core.runtime.DependencySelector
import com.xiaoju.automarket.paladin.core.util.ReflectionUtil
import org.slf4j.{Logger, LoggerFactory}
import scala.jdk.CollectionConverters._
import scala.collection.parallel.CollectionConverters._

/**
 * @Author Luogh
 * @Date 2020/12/11
 * */
class DependencySelectorTask(private val actionId: Int,
                             private val candidateDependencies: Seq[DependencyDescriptor],
                             private val dependencySelector: DependencySelector)
  extends Actor {

  import DependencySelectorTask._



  override def preStart(): Unit = {
    // 注册Dependency

  }

  override def receive: Receive = {
    case event: Event =>
    case other@_ =>
      log.warn(s"received unhandled message: $other, ignore it.")
  }
}

object DependencySelectorTask {

  private val log: Logger = LoggerFactory.getLogger(classOf[DependencySelectorTask])

  def apply(action: ActionDescriptor): Props = {
    val dependencySelector = ReflectionUtil.newInstance(action.getDependencySelector)
    Props.create(classOf[DependencySelectorTask], action.getActionId, action.getDownstreamDependencies.asScala, dependencySelector)
  }
}
