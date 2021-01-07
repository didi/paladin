package com.xiaoju.automarket.paladin.core.runtime.util

import java.util.UUID

import com.xiaoju.automarket.paladin.core.dcg.{ActionDescriptor, ActionHandler, DependencyDescriptor, JobGraphDescriptor, SourceDescriptor}

/**
 * @Author Luogh
 * @Date 2020/12/17
 * */
object Util {

  def generateUUID: String = UUID.randomUUID().toString.replaceAll("-", "")


  def traverseGraph(graph: JobGraphDescriptor,
                    sourceHandler: SourceDescriptor => Unit,
                    actionHandler: ActionDescriptor => Unit,
                    edgeHandler: DependencyDescriptor => Unit
                   ): Unit = {


  }
}
