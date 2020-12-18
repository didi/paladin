package com.xiaoju.automarket.paladin.core.runtime.util

import java.util.UUID

/**
 * @Author Luogh
 * @Date 2020/12/17
 * */
object Util {

  def generateUUID: String = UUID.randomUUID().toString.replaceAll("-", "")
}
