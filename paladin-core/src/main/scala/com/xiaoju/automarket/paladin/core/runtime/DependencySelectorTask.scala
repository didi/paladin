package com.xiaoju.automarket.paladin.core.runtime

import akka.actor.{Actor, ActorRef, Props}
import akka.pattern.ask
import akka.util.Timeout
import com.xiaoju.automarket.paladin.core.common.Event
import com.xiaoju.automarket.paladin.core.dcg.{ActionDescriptor, DependencyDescriptor}
import com.xiaoju.automarket.paladin.core.util.ReflectionUtil
import org.slf4j.{Logger, LoggerFactory}

import scala.collection.mutable.{Map => MMap}
import scala.concurrent.duration._
import scala.concurrent.{ExecutionContextExecutor, Future}
import scala.jdk.CollectionConverters._
import scala.util.{Failure, Success}
import scala.language.postfixOps

/**
 * @Author Luogh
 * @Date 2020/12/11
 * */
class DependencySelectorTask(
                              private val env: JobEnvironment,
                              private val actionId: Int,
                              private val dependencySelector: DependencySelector,
                              private val candidateDependencyDescriptors: Seq[DependencyDescriptor]
                            ) extends Actor {

  import DependencySelectorTask._

  private val candidateDependencyMap: MMap[Int, DependencyLocation] = MMap.empty[Int, DependencyLocation]

  override def preStart(): Unit = {
    // 注册Dependency
    candidateDependencyMap ++= candidateDependencyDescriptors.map { dependency =>
      dependency.getDependencyId -> DependencyLocation(context.actorOf(DependencyTask(env, dependency)), dependency)
    }
    log.info(s"dependency selector registered ${candidateDependencyMap.size} dependency descriptors for action: ${actionId} ")
  }

  override def receive: Receive = {
    case event: Event =>
      val curSender: ActorRef = sender()
      implicit val ec: ExecutionContextExecutor = context.system.dispatcher
      implicit val timeout: Timeout = Timeout(120 seconds)

      val dependencies: Seq[Future[DependencyResponse]] = candidateDependencyMap.values.toSeq.map(_.actorRef).map(dependency => (dependency ? event).mapTo[DependencyResponse])
      Future.sequence(dependencies).map(dependencies => dependencies.filter(_.matched))
        .map { candidateDependencies =>
          candidateDependencies.map(candidate => {
            require(candidateDependencyMap.contains(candidate.dependencyId), s"invalid dependency response: ${candidate}, not found in registered candidate dependency map.")
            candidateDependencyMap(candidate.dependencyId)
          })
        }.onComplete {
        case Success(dp) =>
          val result = dependencySelector.select(event, dp.map(_.dependencyDescriptor).asJava)
          if (result != null) {
            val converted = result.asScala
              .map(d => candidateDependencyMap.getOrElse(d.getDependencyId, throw new RuntimeException(s"invalid dependency: ${d.getDependencyId},not found.")))
              .toSeq
            curSender ! DependencySelectorResponse(converted, event.getEventId)
          } else {
            curSender ! DependencySelectorResponse(Seq.empty, event.getEventId)
          }
        case Failure(exception) =>
          throw new RuntimeException(s"select dependencies failed for action: ${actionId}", exception)
      }

    case other@_ =>
      log.warn(s"received unhandled message: $other, ignore it.")
  }
}

object DependencySelectorTask {

  private val log: Logger = LoggerFactory.getLogger(classOf[DependencySelectorTask])

  def apply(env: JobEnvironment, action: ActionDescriptor): Props = {
    val dependencySelector = ReflectionUtil.newInstance(action.getDependencySelector)
    Props(classOf[DependencySelectorTask], env, action.getActionId, dependencySelector, action.getDownstreamDependencies.asScala.toSeq)
  }
}
