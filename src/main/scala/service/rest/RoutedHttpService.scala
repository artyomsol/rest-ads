package service.rest

import akka.http.scaladsl.server.Directives._
import service.rest.routes.ADServiceRoute

/**
 * Project: rest-ads
 * Package: service.rest
 * Created by asoloviov on 6/27/17 9:36 PM.
 */
trait RoutedHttpService {
  val adServiceRouter: ADServiceRoute
  val routes = pathPrefix("v1") {
    adServiceRouter.route
  }
}
