package service.rest

import service.rest.routes.ADServiceRoute
import service.services.ADService

import scala.concurrent.ExecutionContext

/**
 * Project: rest-ads
 * Package: service.rest
 * Created by asoloviov on 6/27/17 6:43 PM.
 */
class HttpService(adService: ADService)(implicit ec: ExecutionContext) extends RoutedHttpService {
  val adServiceRouter = new ADServiceRoute(adService)
}
