package service.utils

import org.joda.time.format.DateTimeFormat
import org.joda.time.{DateTime, DateTimeZone}
import spray.json._

import scala.util.{Failure, Success, Try}

/**
 * Project: rest-ads
 * Package: service.utils
 * Created by asoloviov on 6/27/17 5:27 PM.
 */
object JodaDateTimeExtensions {
  val pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZ"
  val format = DateTimeFormat.forPattern(pattern)

  implicit val jodaUTCDateTimeFormat = new JsonFormat[DateTime] {
    override def read(json: JsValue): DateTime = json match {
      case JsString(s) => Try(DateTime.parse(s, format)) match {
        case Success(dt) => dt.toDateTime(DateTimeZone.UTC)
        case Failure(t) => deserializationError("DateTime field deserialization error:", t)
      }
      case _ => deserializationError("DateTime field expected")
    }

    override def write(obj: DateTime): JsValue = JsString(obj.toString(format))
  }
}