package com.xiaoju.automarket.paladin.core.runtime.scheduler

import akka.actor.ActorRef
import com.xiaoju.automarket.paladin.core.runtime.JobSchedulerId

/**
 * @Author Luogh
 * @Date 2020/12/18
 * */
case class JobSchedulerInstance(schedulerId: JobSchedulerId, actorRef: ActorRef)
