package com.xiaoju.automarket.paladin.core.runtime.dcg

import com.xiaoju.automarket.paladin.core.dcg.{BizEvent, ConditionHandler}
import com.xiaoju.automarket.paladin.core.runtime.common.Configuration

/**
 * @Author Luogh
 * @Date 2021/1/5
 * */
class DummyConditionHandler extends ConditionHandler {
  override def initialize(config: Configuration): Unit = {}

  override def test(event: BizEvent): Boolean = true

  override def stop(): Unit = {
  }

}
