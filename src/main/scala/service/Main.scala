package service

import akka.http.scaladsl.Http
import service.app.ActorSystemApp
import service.rest.HttpService
import service.services.ADService
import service.utils.EventualDBContextFactory

/**
 * Project: rest-ads
 * Package: service.utils
 * Created by asoloviov on 6/29/17 10:32 PM.
 */
object Main extends App with ActorSystemApp {
  val dbContextFactory = new EventualDBContextFactory(cnf.dbInitializationTimeout)
  val adService = new ADService(dbContextFactory.getDBContext)
  val httpService = new HttpService(adService)

  Http().bindAndHandle(httpService.route, cnf.httpHost, cnf.httpPort)

  override def shutdownProcedure(): Unit = ()
}
