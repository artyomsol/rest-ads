package service.model

import org.joda.time.DateTime
import service.model.FuelType._

/**
 * Project: rest-ads
 * Package: service.model
 * Created by asoloviov on 6/27/17 3:07 PM.
 */
case class AdEntity(id: Long, title: String, fuel: FuelType, price: Int, `new`: Boolean, mileage: Option[Int] = None, firstRegistration: Option[DateTime] = None) {
  require(!title.isEmpty, "title.empty")
  require(`new` == mileage.isEmpty, "new.car.mileage.defined")
  require(`new` == firstRegistration.isEmpty, "new.car.firstRegistration.defined")
}

object AdEntity {
}