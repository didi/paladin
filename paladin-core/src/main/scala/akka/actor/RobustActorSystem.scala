package akka.actor

import java.lang.Thread.UncaughtExceptionHandler

import akka.actor.ActorSystem.findClassLoader
import akka.actor.setup.ActorSystemSetup
import com.typesafe.config.{Config, ConfigFactory}

import scala.concurrent.ExecutionContext

/**
 * [[ActorSystemImpl]] which has a configurable [[java.lang.Thread.UncaughtExceptionHandler]].
 */
class RobustActorSystem(
                         name: String,
                         applicationConfig: Config,
                         classLoader: ClassLoader,
                         defaultExecutionContext: Option[ExecutionContext],
                         guardianProps: Option[Props],
                         setup: ActorSystemSetup,
                         val optionalUncaughtExceptionHandler: Option[UncaughtExceptionHandler])
  extends ActorSystemImpl(
    name,
    applicationConfig,
    classLoader,
    defaultExecutionContext,
    guardianProps,
    setup) {

  override protected def uncaughtExceptionHandler: Thread.UncaughtExceptionHandler =
    optionalUncaughtExceptionHandler.getOrElse(super.uncaughtExceptionHandler)
}

object RobustActorSystem {
  def create(name: String, applicationConfig: Config): RobustActorSystem = {
    apply(name, ActorSystemSetup.create(BootstrapSetup(None, Option(applicationConfig), None)))
  }

  def create(
              name: String,
              applicationConfig: Config,
              uncaughtExceptionHandler: UncaughtExceptionHandler): RobustActorSystem = {
    apply(
      name,
      ActorSystemSetup.create(BootstrapSetup(None, Option(applicationConfig), None)),
      uncaughtExceptionHandler
    )
  }

  def apply(name: String, setup: ActorSystemSetup): RobustActorSystem = {
    internalApply(name, setup, Some(FataExitExceptionHandler()))
  }

  def apply(
             name: String,
             setup: ActorSystemSetup,
             uncaughtExceptionHandler: UncaughtExceptionHandler): RobustActorSystem = {
    internalApply(name, setup, Some(uncaughtExceptionHandler))
  }

  def internalApply(
                     name: String,
                     setup: ActorSystemSetup,
                     uncaughtExceptionHandler: Option[UncaughtExceptionHandler]): RobustActorSystem = {
    val bootstrapSettings = setup.get[BootstrapSetup]
    val cl = bootstrapSettings.flatMap(_.classLoader).getOrElse(findClassLoader())
    val appConfig = bootstrapSettings.flatMap(_.config).getOrElse(ConfigFactory.load(cl))
    val defaultEC = bootstrapSettings.flatMap(_.defaultExecutionContext)

    new RobustActorSystem(
      name,
      appConfig,
      cl,
      defaultEC,
      None,
      setup,
      uncaughtExceptionHandler).start()
  }
}
