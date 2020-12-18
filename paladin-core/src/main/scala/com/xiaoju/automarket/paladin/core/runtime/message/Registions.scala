package com.xiaoju.automarket.paladin.core.runtime.message

import com.xiaoju.automarket.paladin.core.runtime.{JobId, JobExecutorId, TaskAttemptId, TaskId}

/**
 * @Author Luogh
 * @Date 2020/12/18
 * */
case class JobExecutorRegistration(jobExecutorId: JobExecutorId, jobId: JobId)

case class JobExecutorUnRegistration(jobExecutorId: JobExecutorId, jobId: JobId)

case class TaskAttemptRegistration(taskAttemptId: TaskAttemptId, taskId: TaskId)
