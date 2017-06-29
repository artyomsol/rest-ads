package service.utils

import akka.http.scaladsl.model.{HttpResponse, StatusCode, StatusCodes}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.ExceptionHandler

/**
 * Project: rest-ads
 * Package: service.utils
 * Created by asoloviov on 6/30/17 12:25 AM.
 */
sealed trait ServiceException extends Throwable {
  val message: String

  def toStatusCode: StatusCode
}

final case class DataNotFoundException(message: String) extends ServiceException {
  override def toStatusCode: StatusCode = StatusCodes.NotFound
}

object ServiceException {
  implicit def serviceExceptionHandler: ExceptionHandler =
    ExceptionHandler {
      case e: ServiceException =>
        extractUri { uri => complete(HttpResponse(e.toStatusCode, entity = e.message)) }
    }
}
