package service.app

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import app.BuildInfo
import service.utils.{AppConfig, AppSignalHandler}

import scala.concurrent.ExecutionContext

/**
 * Project: rest-ads
 * Package: service.app
 * Created by asoloviov on 6/29/17 10:35 PM.
 */
trait ActorSystemApp {
  protected val signalHandler = AppSignalHandler()
  implicit val cnf: AppConfig = try AppConfig() catch {
    case t: Throwable => throw new IllegalArgumentException("Configuration error:" + t.getMessage)
  }
  implicit val system = ActorSystem(cnf.actorSystemName, cnf.config)
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val executionContext: ExecutionContext = system.dispatcher

  def shutdownProcedure(): Unit

  val log = system.log
  system.registerOnTermination(shutdownProcedure())
  signalHandler.registerReaper(shutdownProcedure())

  log.info("Actor system  start: " + BuildInfo)

  sys.addShutdownHook {
    log.info("Actor system shutdown")
    system.terminate() //shutdown()
  }
}