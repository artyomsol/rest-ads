package service.rest

import akka.http.scaladsl.server.Directives._
import service.rest.routes.ADServiceRoute
import service.utils.ServiceException

/**
 * Project: rest-ads
 * Package: service.rest
 * Created by asoloviov on 6/27/17 9:36 PM.
 */
trait RoutedHttpService extends CorsSupport {
  val adServiceRouter: ADServiceRoute

  def route = pathPrefix("v1") {
    corsEnabled {
      handleExceptions(ServiceException.serviceExceptionHandler) {
        adServiceRouter.route
      }
    }
  }
}
