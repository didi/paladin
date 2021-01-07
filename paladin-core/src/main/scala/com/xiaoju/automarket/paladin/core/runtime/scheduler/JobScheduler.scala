package com.xiaoju.automarket.paladin.core.runtime.scheduler

import akka.actor.{Actor, ActorLogging, ActorRef, PoisonPill, Props}
import akka.util.Timeout
import com.xiaoju.automarket.paladin.core.common.ExecutionStateEnum
import com.xiaoju.automarket.paladin.core.runtime.common.Environment
import com.xiaoju.automarket.paladin.core.runtime.executor.JobExecutor
import com.xiaoju.automarket.paladin.core.runtime.message._
import com.xiaoju.automarket.paladin.core.runtime.scheduler.JobScheduler.JobSchedulerInstance
import com.xiaoju.automarket.paladin.core.runtime.util.Util
import com.xiaoju.automarket.paladin.core.runtime.{JobId, JobSchedulerId, scheduler}

import scala.collection.mutable.{Map => MMap}
import scala.concurrent.ExecutionContextExecutor

/**
 * @Author Luogh
 * @Date 2020/12/17
 * */
class JobScheduler(val env: Environment) extends Actor with ActorLogging {

  implicit val DEFAULT_TIMEOUT: Timeout = Timeout(env.config.requestTimeout())
  implicit val DEFAULT_EXECUTION_CONTEXT: ExecutionContextExecutor = context.dispatcher

  private var jobSchedulerInstance: JobSchedulerInstance = _
  private val allSubmittedJobs: MMap[JobId, JobInstance] = MMap.empty

  override def preStart(): Unit = {
    this.jobSchedulerInstance = JobSchedulerInstance(Util.generateUUID, self)
  }

  override def receive: Receive = {
    case JobSubmitRequest(graphDescriptor) =>
      val jobId = Util.generateUUID
      val jobExecutorActorRef = context.system.actorOf(JobExecutor.props(env, jobId, graphDescriptor, jobSchedulerInstance))
      allSubmittedJobs += jobId -> scheduler.JobInstance(jobId, jobExecutorActorRef, graphDescriptor, ExecutionStateEnum.SUBMITTED)

    case JobExecutorRegistration(_, jobId) =>
      if (allSubmittedJobs.contains(jobId)) {
        val jobWithNewState = allSubmittedJobs(jobId).copy(executionStateEnum = ExecutionStateEnum.DEPLOYED)
        allSubmittedJobs += jobId -> jobWithNewState
        jobWithNewState.jobExecutorActorRef ! JobTaskCreateRequest
      }

    case JobFailure(jobExecutorId, jobId, throwable, msg) =>
      log.info(s"job: $jobId failed with exception: $throwable, msg = $msg, trying to stop job executor:$jobExecutorId")
      allSubmittedJobs -= jobId
      sender() ! PoisonPill

    case JobCancelRequest(jobId) =>
    // TODO
    case JobStatusRequest(jobId) =>
    // TODO
  }


}

object JobScheduler {

  def props(env: Environment): Props = Props(new JobScheduler(env))

  case class JobSchedulerInstance(schedulerId: JobSchedulerId, actorRef: ActorRef)

}
