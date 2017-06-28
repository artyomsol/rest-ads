package service.utils

import java.util.UUID

import spray.json._


/**
 * Project: rest-ads
 * Package: service.utils
 * Created by asoloviov on 6/28/17 8:49 PM.
 */
object UUIDExtensions {


  implicit val uuidFormat = new JsonFormat[UUID] {
    override def read(json: JsValue): UUID = json match {
      case JsString(s) => UUID.fromString(s)
      case other => deserializationError("Expected UUID as JsString, but got " + other)
    }

    override def write(obj: UUID): JsValue = JsString(obj.toString)
  }
}
