package com.xiaoju.automarket.paladin.core.runtime.scheduler

import akka.actor.ActorRef
import com.xiaoju.automarket.paladin.core.common.ExecutionStateEnum
import com.xiaoju.automarket.paladin.core.dcg.JobGraphDescriptor
import com.xiaoju.automarket.paladin.core.runtime.{JobId, TaskId}

/**
 * @Author Luogh
 * @Date 2020/12/17
 * */
case class JobInstance(jobID: JobId,
                       jobExecutorActorRef: ActorRef,
                       jobGraph: JobGraphDescriptor,
                       executionStateEnum: ExecutionStateEnum,
                       taskSets: Set[TaskId] = Set.empty)