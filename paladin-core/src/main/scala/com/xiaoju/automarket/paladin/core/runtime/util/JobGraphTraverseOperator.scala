package com.xiaoju.automarket.paladin.core.runtime.util

import com.xiaoju.automarket.paladin.core.dcg.{DependencyDescriptor, JobGraphDescriptor, VertexDescriptor}
import com.xiaoju.automarket.paladin.core.runtime.TaskId

import scala.collection.mutable.{Set => MSet}
import scala.jdk.CollectionConverters.CollectionHasAsScala

/**
 * @Author Luogh
 * @Date 2021/1/5
 * */
object JobGraphTraverseOperator {

  def traverseInDepth(jobGraphDescriptor: JobGraphDescriptor, graphObserver: GraphVisitor): Unit = {
    val visitMap: MSet[String] = MSet.empty

    def dfs(vertex: VertexDescriptor[_]): Unit = {
      if (vertex != null && !visitMap.contains(vertex.vertexId())) {
        graphObserver.onVertex(vertex.vertexId(), vertex)
        visitMap += vertex.vertexId()

        val dependencies = vertex.downstreamDependencies()
        if (dependencies != null && !dependencies.isEmpty) {
          for (dependency <- dependencies.asScala) {
            if (!visitMap.contains(dependency.getDependencyId)) {
              visitMap += dependency.getDependencyId
              dfs(dependency.getNextVertexDescriptor)
              graphObserver.onEdge(dependency.getDependencyId, dependency)
            }
          }
        }
      }
    }

    for (sourceDescriptor <- jobGraphDescriptor.getSources.asScala) {
      val sourceId = sourceDescriptor.vertexId()
      if (!visitMap.contains(sourceId)) {
        graphObserver.onVertex(sourceId, sourceDescriptor)
        visitMap += sourceId
        val dependencies = sourceDescriptor.getDownstreamDependencies
        if (dependencies != null && !dependencies.isEmpty) {
          for (dependency <- dependencies.asScala) {
            if (!visitMap.contains(dependency.getDependencyId)) {
              visitMap += dependency.getDependencyId
              dfs(dependency.getNextVertexDescriptor)
              graphObserver.onEdge(dependency.getDependencyId, dependency)
            }
          }
        }
      }
    }
  }


  trait GraphVisitor {
    def onVertex(taskId: TaskId, descriptor: VertexDescriptor[_])

    def onEdge(taskId: TaskId, descriptor: DependencyDescriptor)
  }


}