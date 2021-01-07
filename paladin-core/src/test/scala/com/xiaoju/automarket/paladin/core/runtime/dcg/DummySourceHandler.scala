package com.xiaoju.automarket.paladin.core.runtime.dcg

import com.xiaoju.automarket.paladin.core.dcg.{BizEvent, SourceHandler}
import com.xiaoju.automarket.paladin.core.runtime.common.Configuration

/**
 * @Author Luogh
 * @Date 2021/1/5
 * */
class DummySourceHandler extends SourceHandler {
  override def initialize(config: Configuration): Unit = {
    println("init source")
  }

  override def hashNext(): Boolean = true

  override def next(): BizEvent = new BizEvent

  override def stop(): Unit = {}
}
