package com.xiaoju.automarket.paladin.core.runtime

import akka.actor.ActorRef
import com.xiaoju.automarket.paladin.core.dcg.DependencyDescriptor

/**
 * @Author Luogh
 * @Date 2020/12/11
 * */
case class DependencySelectorResponse(dependencies: Seq[DependencyLocation], eventId: String)

case class DependencyLocation(actorRef: ActorRef, dependencyDescriptor: DependencyDescriptor)

case class DependencyResponse(dependencyId: Int, matched: Boolean)

case class DependencySelectorLocation(actorRef: ActorRef)

case class ActionLocation(actionId: Int, actorRef: ActorRef)



