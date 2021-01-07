package com.xiaoju.automarket.paladin.core.runtime.dcg

import com.xiaoju.automarket.paladin.core.dcg.{ActionHandler, BizEvent}
import com.xiaoju.automarket.paladin.core.runtime.common.Configuration

/**
 * @Author Luogh
 * @Date 2021/1/5
 * */
class DummyActionHandler extends ActionHandler {
  override def initialize(config: Configuration): Unit = {}

  override def doAction(input: BizEvent, collector: ActionHandler.Collector): Unit = {
    collector.collect(input)
  }

  override def stop(): Unit = {}
}
