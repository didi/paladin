package com.xiaoju.automarket.paladin.core.runtime.util

import com.xiaoju.automarket.paladin.core.dcg.{ActionDescriptor, DependencyDescriptor, JobGraphDescriptor, SourceDescriptor, VertexDescriptor}
import com.xiaoju.automarket.paladin.core.runtime.TaskId
import com.xiaoju.automarket.paladin.core.runtime.dcg.{DummyActionHandler, DummyConditionHandler, DummySourceHandler}
import org.scalatest.funsuite.AnyFunSuite

import scala.jdk.CollectionConverters.SeqHasAsJava


/**
 * @Author Luogh
 * @Date 2021/1/5
 * */
class JobGraphTraverseOperatorTest extends AnyFunSuite {

  test("test job graph traverse") {
    val jobGraph = new JobGraphDescriptor()
    val source1 = buildSourceDescriptor("S-01")
    val action1 = buildActionDescriptor("A-01")
    val dependency1 = buildDependencyDescriptor("<S-01,A-01>", source1, action1)

    val action2 = buildActionDescriptor("A-02")
    val dependency3 = buildDependencyDescriptor("<S-01,A-02>", source1, action2)
    source1.setDownstreamDependencies(Seq(dependency1, dependency3).asJava)

    val action3 = buildActionDescriptor("A-03")
    val dependency4 = buildDependencyDescriptor("<A-02,A-03>", action2, action3)
    action2.setDownstreamDependencies(Seq(dependency4).asJava)
    action2.setUpstreamDependencies(Seq(dependency3).asJava)
    action3.setDownstreamDependencies(Seq(dependency4).asJava)

    val source2 = buildSourceDescriptor("S-02")
    jobGraph.setSources(Seq(source1, source2).asJava)

    // S-01 => A-01
    // S-01 => A-02 => A-03

    JobGraphTraverseOperator.traverseInDepth(jobGraph, new JobGraphTraverseOperator.GraphVisitor {
      override def onVertex(taskId: TaskId, descriptor: VertexDescriptor[_]): Unit = {
        println(s"vertex: ${taskId} with name: ${descriptor.vertexName()}")
      }

      override def onEdge(taskId: TaskId, descriptor: DependencyDescriptor): Unit = {
        println(s"dependency: ${taskId} with name: ${descriptor.getDependencyName()}")
      }
    })

  }

  private def buildSourceDescriptor(name: String): SourceDescriptor =
    new SourceDescriptor(name, new DummySourceHandler)

  private def buildActionDescriptor(name: String): ActionDescriptor =
    new ActionDescriptor(name, new DummyActionHandler)

  private def buildDependencyDescriptor(name: String, in: VertexDescriptor[_], out: VertexDescriptor[_]): DependencyDescriptor =
    new DependencyDescriptor(name, new DummyConditionHandler, in, out)

}


