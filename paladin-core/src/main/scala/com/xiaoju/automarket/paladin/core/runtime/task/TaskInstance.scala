package com.xiaoju.automarket.paladin.core.runtime.task

import com.xiaoju.automarket.paladin.core.dcg.{ActionDescriptor, DependencyDescriptor, SourceDescriptor}
import com.xiaoju.automarket.paladin.core.runtime.{JobId, TaskId}

/**
 * @Author Luogh
 * @Date 2020/12/17
 * */
sealed trait TaskInstance {
  type TaskDescriptor

  def getTaskDescriptor: TaskDescriptor

  def getTaskId: TaskId

  def getJobId: JobId
}

case class SourceTaskInstance(
                               jobId: JobId, taskId: TaskId,
                               sourceDescriptor: SourceDescriptor
                             ) extends TaskInstance {
  override type TaskDescriptor = SourceDescriptor

  override def getTaskDescriptor: SourceDescriptor = sourceDescriptor

  override def getTaskId: TaskId = taskId

  override def getJobId: JobId = jobId
}

case class ActionTaskInstance(jobId: JobId, taskId: TaskId,
                              actionDescriptor: ActionDescriptor
                             ) extends TaskInstance {
  override type TaskDescriptor = ActionDescriptor

  override def getTaskDescriptor: ActionDescriptor = this.actionDescriptor

  override def getTaskId: TaskId = this.taskId

  override def getJobId: JobId = this.jobId
}

case class DependencyTaskInstance(jobId: JobId, taskId: TaskId,
                                  dependencyDescriptor: DependencyDescriptor
                                 ) extends TaskInstance {
  override type TaskDescriptor = DependencyDescriptor

  override def getTaskDescriptor: DependencyDescriptor = this.dependencyDescriptor

  override def getTaskId: TaskId = this.taskId

  override def getJobId: JobId = this.jobId
}

