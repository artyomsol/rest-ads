package service.model

import spray.json.{DeserializationException, JsString, JsValue, JsonFormat}

/**
 * Project: rest-ads
 * Package: service.model
 * Created by asoloviov on 6/27/17 3:12 PM.
 */
object FuelType extends Enumeration {
  type FuelType = Value
  val gasoline, diesel = Value

  implicit val fuelTypeFormat = new JsonFormat[FuelType] {
    override def write(obj: FuelType): JsValue = JsString(obj.toString)

    override def read(json: JsValue): FuelType = json match {
      case JsString(value) => FuelType.withName(value)
      case other => throw DeserializationException(s"Expected fuel type but got $other")
    }
  }
}
