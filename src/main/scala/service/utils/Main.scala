package service.utils

import akka.http.scaladsl.Http
import service.app.ActorSystemApp
import service.rest.HttpService
import service.services.ADService
import service.utils.db.DBContext

/**
 * Project: rest-ads
 * Package: service.utils
 * Created by asoloviov on 6/29/17 10:32 PM.
 */
object Main extends App with ActorSystemApp {
  implicit val dbContext = try DBContext(cnf).init(cnf.dbInitializationTimeout) catch {
    case t: Throwable =>
      system.terminate()
      throw new IllegalStateException("DBContext creation error:" + t.getMessage, t)
  }
  val adService = new ADService()
  val httpService = new HttpService(adService)

  Http().bindAndHandle(httpService.route, cnf.httpHost, cnf.httpPort)

  override def shutdownProcedure(): Unit = ()
}
