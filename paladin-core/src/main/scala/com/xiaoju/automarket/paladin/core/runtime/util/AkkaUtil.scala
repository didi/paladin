/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.xiaoju.automarket.paladin.core.runtime.util

import java.io.IOException
import java.net._
import java.util.concurrent.{Callable, CompletableFuture}

import akka.actor._
import akka.pattern.{ask => akkaAsk}
import com.typesafe.config.Config
import com.xiaoju.automarket.paladin.core.util.Util
import org.slf4j.{Logger, LoggerFactory}

import scala.annotation.tailrec
import scala.compat.java8.FutureConverters
import scala.concurrent._
import scala.concurrent.duration._
import scala.language.postfixOps

/**
 * This class contains utility functions for akka. It contains methods to start an actor system with
 * a given akka configuration. Furthermore, the akka configuration used for starting the different
 * actor systems resides in this class.
 */
object AkkaUtil {
  val LOG: Logger = LoggerFactory.getLogger(AkkaUtil.getClass)

  val ACTOR_SYSTEM_NAME = "paladin"

  def getPaladinActorSystemName = {
    ACTOR_SYSTEM_NAME
  }

  /**
   * Creates a local actor system without remoting.
   *
   * @param configuration instance containing the user provided configuration values
   * @return The created actor system
   */
  def createLocalActorSystem(configuration: Config): ActorSystem = {
    createActorSystem(configuration)
  }

  /**
   * Creates an actor system bound to the given hostname and port.
   *
   * @param configuration instance containing the user provided configuration values
   * @param hostname      of the network interface to bind to
   * @param port          of to bind to
   * @return created actor system
   */
  def createActorSystem(
                         configuration: Config,
                         hostname: String,
                         port: Int)
  : ActorSystem = {

    createActorSystem(configuration, Some((hostname, port)))
  }

  /**
   * Creates an actor system. If a listening address is specified, then the actor system will listen
   * on that address for messages from a remote actor system. If not, then a local actor system
   * will be instantiated.
   *
   * @param configuration    instance containing the user provided configuration values
   * @param listeningAddress an optional tuple containing a bindAddress and a port to bind to.
   *                         If the parameter is None, then a local actor system will be created.
   * @return created actor system
   */
  def createActorSystem(
                         configuration: Config,
                         listeningAddress: Option[(String, Int)])
  : ActorSystem = {
    createActorSystem(configuration)
  }

  /**
   * Creates an actor system with the given akka config.
   *
   * @param akkaConfig configuration for the actor system
   * @return created actor system
   */
  def createActorSystem(akkaConfig: Config): ActorSystem = {
    createActorSystem(ACTOR_SYSTEM_NAME, akkaConfig)
  }

  /**
   * Creates an actor system with the given akka config.
   *
   * @param akkaConfig configuration for the actor system
   * @return created actor system
   */
  def createActorSystem(actorSystemName: String, akkaConfig: Config): ActorSystem = {
    RobustActorSystem.create(actorSystemName, akkaConfig)
  }


  /** Returns a [[Future]] to the [[ActorRef]] of the child of a given actor. The child is specified
   * by providing its actor name.
   *
   * @param parent  [[ActorRef]] to the parent of the child to be retrieved
   * @param child   Name of the child actor
   * @param system  [[ActorSystem]] to be used
   * @param timeout Maximum timeout for the future
   * @return [[Future]] to the [[ActorRef]] of the child actor
   */
  def getChild(
                parent: ActorRef,
                child: String,
                system: ActorSystem,
                timeout: FiniteDuration)
  : Future[ActorRef] = {
    system.actorSelection(parent.path / child).resolveOne()(timeout)
  }

  /** Returns a [[Future]] to the [[ActorRef]] of an actor. The actor is specified by its path.
   *
   * @param path    Path to the actor to be retrieved
   * @param system  [[ActorSystem]] to be used
   * @param timeout Maximum timeout for the future
   * @return [[Future]] to the [[ActorRef]] of the actor
   */
  def getActorRefFuture(
                         path: String,
                         system: ActorSystem,
                         timeout: FiniteDuration)
  : Future[ActorRef] = {
    system.actorSelection(path).resolveOne()(timeout)
  }

  /** Returns an [[ActorRef]] for the actor specified by the path parameter.
   *
   * @param path    Path to the actor to be retrieved
   * @param system  [[ActorSystem]] to be used
   * @param timeout Maximum timeout for the future
   * @throws java.io.IOException
   * @return [[ActorRef]] of the requested [[Actor]]
   */
  @throws(classOf[IOException])
  def getActorRef(
                   path: String,
                   system: ActorSystem,
                   timeout: FiniteDuration)
  : ActorRef = {
    try {
      val future = AkkaUtil.getActorRefFuture(path, system, timeout)
      Await.result(future, timeout)
    }
    catch {
      case e@(_: ActorNotFound | _: TimeoutException) =>
        throw new IOException(
          s"Actor at $path not reachable. " +
            "Please make sure that the actor is running and its port is reachable.", e)

      case e: IOException =>
        throw new IOException(s"Could not connect to the actor at $path", e)
    }
  }


  /**
   * Utility function to construct a future which tries multiple times to execute itself if it
   * fails. If the maximum number of tries are exceeded, then the future fails.
   *
   * @param body             function describing the future action
   * @param tries            number of maximum tries before the future fails
   * @param executionContext which shall execute the future
   * @tparam T return type of the future
   * @return future which tries to recover by re-executing itself a given number of times
   */
  def retry[T](body: => T, tries: Int)(implicit executionContext: ExecutionContext): Future[T] = {
    Future {
      body
    }.recoverWith {
      case t: Throwable =>
        if (tries > 0) {
          retry(body, tries - 1)
        } else {
          Future.failed(t)
        }
    }
  }

  /**
   * Utility function to construct a future which tries multiple times to execute itself if it
   * fails. If the maximum number of tries are exceeded, then the future fails.
   *
   * @param callable         future action
   * @param tries            maximum number of tries before the future fails
   * @param executionContext which shall execute the future
   * @tparam T return type of the future
   * @return future which tries to recover by re-executing itself a given number of times
   */
  def retry[T](callable: Callable[T], tries: Int)(implicit executionContext: ExecutionContext):
  Future[T] = {
    retry(callable.call(), tries)
  }

  /**
   * Utility function to construct a future which tries multiple times to execute itself if it
   * fails. If the maximum number of tries are exceeded, then the future fails.
   *
   * @param target           actor which receives the message
   * @param message          to be sent to the target actor
   * @param tries            maximum number of tries before the future fails
   * @param executionContext which shall execute the future
   * @param timeout          of the future
   * @return future which tries to recover by re-executing itself a given number of times
   */
  def retry(target: ActorRef, message: Any, tries: Int)(implicit executionContext:
  ExecutionContext, timeout: FiniteDuration): Future[Any] = {
    (target ? message) (timeout) recoverWith {
      case t: Throwable =>
        if (tries > 0) {
          retry(target, message, tries - 1)
        } else {
          Future.failed(t)
        }
    }
  }

  /** Returns the address of the given [[ActorSystem]]. The [[Address]] object contains
   * the port and the host under which the actor system is reachable
   *
   * @param system [[ActorSystem]] for which the [[Address]] shall be retrieved
   * @return [[Address]] of the given [[ActorSystem]]
   */
  def getAddress(system: ActorSystem): Address = {
    RemoteAddressExtension(system).address
  }

  /** Returns the given [[ActorRef]]'s path string representation with host and port of the
   * [[ActorSystem]] in which the actor is running.
   *
   * @param system [[ActorSystem]] in which the given [[ActorRef]] is running
   * @param actor  [[ActorRef]] of the [[Actor]] for which the URL has to be generated
   * @return String containing the [[ActorSystem]] independent URL of the [[Actor]]
   */
  def getAkkaURL(system: ActorSystem, actor: ActorRef): String = {
    val address = getAddress(system)
    actor.path.toStringWithAddress(address)
  }


  /** Returns the AkkaURL for a given [[ActorSystem]] and a path describing a running [[Actor]] in
   * the actor system.
   *
   * @param system [[ActorSystem]] in which the given [[Actor]] is running
   * @param path   Path describing an [[Actor]] for which the URL has to be generated
   * @return String containing the [[ActorSystem]] independent URL of an [[Actor]] specified by
   *         path.
   */
  def getAkkaURL(system: ActorSystem, path: String): String = {
    val address = getAddress(system)
    address.toString + path
  }

  /** Extracts the hostname and the port of the remote actor system from the given Akka URL. The
   * result is an [[InetSocketAddress]] instance containing the extracted hostname and port. If
   * the Akka URL does not contain the hostname and port information, e.g. a local Akka URL is
   * provided, then an [[Exception]] is thrown.
   *
   * @param akkaURL The URL to extract the host and port from.
   * @throws java.lang.Exception Thrown, if the given string does not represent a proper url
   * @return The InetSocketAddress with the extracted host and port.
   */
  @throws(classOf[Exception])
  def getInetSocketAddressFromAkkaURL(akkaURL: String): InetSocketAddress = {
    // AkkaURLs have the form schema://systemName@host:port/.... if it's a remote Akka URL
    try {
      val address = getAddressFromAkkaURL(akkaURL)

      (address.host, address.port) match {
        case (Some(hostname), Some(portValue)) => new InetSocketAddress(hostname, portValue)
        case _ => throw new MalformedURLException()
      }
    }
    catch {
      case _: MalformedURLException =>
        throw new Exception(s"Could not retrieve InetSocketAddress from Akka URL $akkaURL")
    }
  }

  /**
   * Extracts the [[Address]] from the given akka URL.
   *
   * @param akkaURL to extract the [[Address]] from
   * @throws java.net.MalformedURLException if the [[Address]] could not be parsed from
   *                                        the given akka URL
   * @return Extracted [[Address]] from the given akka URL
   */
  @throws(classOf[MalformedURLException])
  def getAddressFromAkkaURL(akkaURL: String): Address = {
    AddressFromURIString(akkaURL)
  }


  /**
   * Retries a function if it fails because of a [[java.net.BindException]].
   *
   * @param fn                     The function to retry
   * @param stopCond               Flag to signal termination
   * @param maxSleepBetweenRetries Max random sleep time between retries
   * @tparam T Return type of the function to retry
   * @return Return value of the function to retry
   */
  @tailrec
  def retryOnBindException[T](
                               fn: => T,
                               stopCond: => Boolean,
                               maxSleepBetweenRetries: Long = 0)
  : scala.util.Try[T] = {

    def sleepBeforeRetry(): Unit = {
      if (maxSleepBetweenRetries > 0) {
        val sleepTime = (Math.random() * maxSleepBetweenRetries).asInstanceOf[Long]
        LOG.info(s"Retrying after bind exception. Sleeping for $sleepTime ms.")
        Thread.sleep(sleepTime)
      }
    }

    scala.util.Try {
      fn
    } match {
      case scala.util.Failure(x: BindException) =>
        if (stopCond) {
          scala.util.Failure(x)
        } else {
          sleepBeforeRetry()
          retryOnBindException(fn, stopCond)
        }
      case scala.util.Failure(x: Exception) => x.getCause match {
        case _ => scala.util.Failure(x)
      }
      case f => f
    }
  }

  /**
   * Terminates the given [[ActorSystem]] and returns its termination future.
   *
   * @param actorSystem to terminate
   * @return Termination future
   */
  def terminateActorSystem(actorSystem: ActorSystem): CompletableFuture[Void] = {
    FutureConverters.toJava(actorSystem.terminate).thenAccept(Util.ignoreFn()).toCompletableFuture
  }
}

