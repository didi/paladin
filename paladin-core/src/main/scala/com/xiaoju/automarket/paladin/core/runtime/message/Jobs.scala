package com.xiaoju.automarket.paladin.core.runtime.message

/**
 * @Author Luogh
 * @Date 2020/12/17
 * */

import com.xiaoju.automarket.paladin.core.dcg.JobGraphDescriptor
import com.xiaoju.automarket.paladin.core.runtime.{JobExecutorId, JobId}

case class JobCancelRequest(jobId: JobId)

case class JobSubmitRequest(jobDescriptor: JobGraphDescriptor)

case class JobStatusRequest(jobId: JobId)

case object JobTaskCreateRequest

case class JobFailure(jobExecutorId: JobExecutorId, jobId: JobId, throwable: Throwable, msg: String = "")

