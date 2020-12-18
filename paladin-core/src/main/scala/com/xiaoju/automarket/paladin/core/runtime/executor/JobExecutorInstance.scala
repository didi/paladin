package com.xiaoju.automarket.paladin.core.runtime.executor

import akka.actor.ActorRef
import com.xiaoju.automarket.paladin.core.runtime.JobExecutorId

/**
 * @Author Luogh
 * @Date 2020/12/18
 * */
case class JobExecutorInstance(jobExecutorId: JobExecutorId, actorRef: ActorRef)
