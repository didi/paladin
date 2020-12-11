package akka.actor


import org.slf4j.{Logger, LoggerFactory}

/**
 * @Author Luogh
 * @Date 2020/12/11
 * */
class FataExitExceptionHandler extends Thread.UncaughtExceptionHandler {

  import akka.actor.FataExitExceptionHandler._

  override def uncaughtException(t: Thread, e: Throwable): Unit = {
    try {
      LOG.error(s"FATAL: Thread '${t.getName}' produced an uncaught exception. Stopping the process...", e)
    } finally {
      System.exit(-1);
    }
  }
}

object FataExitExceptionHandler {

  val LOG: Logger = LoggerFactory.getLogger(classOf[FataExitExceptionHandler])

  def apply(): FataExitExceptionHandler = new FataExitExceptionHandler()
}