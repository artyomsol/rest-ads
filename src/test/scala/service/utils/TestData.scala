package service.utils

import org.joda.time.DateTime
import service.model.FuelType._
import service.model.{ADEntity, ADEntityUpdate, FuelType}

import scala.util.Random

/**
 * Project: rest-ads
 * Package: service.utils
 * Created by asoloviov on 6/28/17 12:05 AM.
 */
trait TestData {
  val oldCarAD = ADEntity(Some("e029819db7b34917a3a277625f3e660e"), "Audi A4 Avant", diesel, 100, `new` = false, mileage = Some(100000), Some(DateTime.parse("2017-06-27T00:00:00.000Z")))
  val updateToOldCarAD = ADEntityUpdate(Some("Audi A4 Avant"), Some(diesel), None, Some(false), Some(100000), Some(DateTime.parse("2017-06-27T00:00:00.000Z")))
  val newCarAD = ADEntity(Some("e029819db7b34917a3a277625f3e660e"), "Audi A4 Avant", diesel, 100, `new` = true)
  val updateAD = ADEntityUpdate(Some(Random.nextString(10)), Some(FuelType.diesel), None, `new` = Some(false), mileage = Some(10001), Some(DateTime.parse("2017-06-27T00:00:00.000Z")))
  val totalUpdateAD = ADEntityUpdate(Some(Random.nextString(10)), Some(FuelType.diesel), Some(10000), `new` = Some(false), mileage = Some(10001), Some(DateTime.parse("2017-06-27T00:00:00.000Z")))
}
