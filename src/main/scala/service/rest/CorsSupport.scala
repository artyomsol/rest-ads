package service.rest

import akka.http.scaladsl.model.HttpMethods._
import akka.http.scaladsl.model.HttpResponse
import akka.http.scaladsl.model.headers._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.{Directive0, Route}
import service.utils.AppConfig

/**
 * Project: rest-ads
 * Package: service.rest
 * Created by asoloviov on 6/29/17 9:38 PM.
 */
trait CorsSupport {
  val cnf: AppConfig

  private def allowedOriginHeader = cnf.allowedOriginOpt.map(allowedOrigin =>
    if (allowedOrigin == "*") `Access-Control-Allow-Origin`.*
    else `Access-Control-Allow-Origin`(HttpOrigin(allowedOrigin))
  ).getOrElse(`Access-Control-Allow-Origin`.`null`)

  private def withAccessControlHeaders: Directive0 =
    mapResponseHeaders { headers =>
      allowedOriginHeader +:
        `Access-Control-Allow-Headers`("Content-Type", "X-Requested-With") +:
        headers
    }

  private def preflightRequestRoute: Route = options {
    complete(
      HttpResponse(200).withHeaders(
        `Access-Control-Allow-Methods`(OPTIONS, POST, GET, DELETE)
      )
    )
  }

  def corsEnabled(r: Route) = cnf.allowedOriginOpt.fold(r)(_ =>
      withAccessControlHeaders {
        preflightRequestRoute ~ r
      }
    )
}
