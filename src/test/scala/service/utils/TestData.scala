package service.utils

import org.joda.time.DateTime
import service.model.ADEntity
import service.model.FuelType._

/**
 * Project: rest-ads
 * Package: service.utils
 * Created by asoloviov on 6/28/17 12:05 AM.
 */
trait TestData {
  val oldCarAD = ADEntity(10L, "Audi A4 Avant", diesel, 100, `new` = false, mileage = Some(100000), Some(DateTime.parse("2017-06-27T00:00:00.000Z")))
  val newCarAD = ADEntity(10L, "Audi A4 Avant", diesel, 100, `new` = true)
}
