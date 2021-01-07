package com.xiaoju.automarket.paladin.core.runtime.util

import akka.actor.{Actor, ActorLogging, ActorSystem, Props}
import org.scalatest.funsuite.AnyFunSuite

/**
 * @Author Luogh
 * @Date 2021/1/5
 * */
class ActorTest extends AnyFunSuite {

  test("test actor init") {
    val actorSystem = ActorSystem("test")
    val actor1 = actorSystem.actorOf(TActor.props())
    val actor2 = actorSystem.actorOf(TActor.props())

    println("asdasdfasdfasd")
    assert(actor1 != actor2)

    Thread.sleep(30_000)
  }


  class TActor extends Actor with ActorLogging {

    override def preStart(): Unit = {
      Thread.sleep(10_000)
      println("...........................")
    }

    override def receive: Receive = {
      case other @ _ => println(other)
    }
  }

  object TActor {
    def props(): Props = Props(new TActor())
  }

}
