package com.xiaoju.automarket.paladin.core.runtime.akka

import akka.actor.ActorRef
import com.xiaoju.automarket.paladin.core.common.Event

/**
 * @Author Luogh
 * @Date 2020/12/11
 * */
case class DependencySelectorResult(dependencies: Seq[DependencyLocation], event: Event)

case class DependencyLocation(dependencyId: Int, actorRef: ActorRef)
