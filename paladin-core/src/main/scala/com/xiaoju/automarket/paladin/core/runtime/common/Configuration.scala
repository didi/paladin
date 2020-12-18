package com.xiaoju.automarket.paladin.core.runtime.common

import com.typesafe.config.Config

import scala.concurrent.duration.{DurationInt, FiniteDuration}

/**
 * @Author Luogh
 * @Date 2020/12/17
 * */
class Configuration(val config: Config) {

  import Configuration._

  def minTaskExecutorNum(): Int =
    getOrDefault[Int](MIN_TASK_EXECUTOR_INSTANCE_KEY, config.getInt, DEFAULT_MIN_TASK_EXECUTOR_INSTANCE)

  def maxTaskExecutorNum(): Int =
    getOrDefault[Int](MAX_TASK_EXECUTOR_INSTANCE_KEY, config.getInt, DEFAULT_MAX_TASK_EXECUTOR_INSTANCE)

  def requestTimeout(): FiniteDuration = 3.seconds

  private def getOrDefault[T](key: String, func: String => T, default: => T): T = {
    if (config.hasPath(key)) {
      func(key)
    } else {
      default
    }
  }
}

object Configuration {

  val DEFAULT_MAX_TASK_EXECUTOR_INSTANCE = 1
  val MAX_TASK_EXECUTOR_INSTANCE_KEY = "task-executor.num.max"
  val DEFAULT_MIN_TASK_EXECUTOR_INSTANCE = 1
  val MIN_TASK_EXECUTOR_INSTANCE_KEY = "task-executor.num.min"

  def apply(config: Config) = {
    require(config != null, "config is null")
    new Configuration(config)
  }
}
