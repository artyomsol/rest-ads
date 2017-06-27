package service.model

import org.joda.time.DateTime
import service.model.FuelType._

/**
 * Project: rest-ads
 * Package: service.model
 * Created by asoloviov on 6/27/17 11:12 PM.
 */
case class ADEntityUpdate(title: Option[String], fuel: Option[FuelType], price: Option[Int], `new`: Option[Boolean], mileage: Option[Int], firstRegistration: Option[DateTime] = None) {
  def applyTo(ad: ADEntity): ADEntity = {
    val resultingUsage: Boolean = `new`.getOrElse(ad.`new`)
    ADEntity(
      ad.id,
      title.getOrElse(ad.title),
      fuel.getOrElse(ad.fuel),
      price.getOrElse(ad.price),
      resultingUsage,
      mileage.orElse(ad.mileage).filterNot(_ => resultingUsage),
      firstRegistration.orElse(ad.firstRegistration).filterNot(_ => resultingUsage)
    )
  }
}

object ADEntityUpdate {

  import service.utils.JodaDateTimeExtensions._
  import spray.json.DefaultJsonProtocol._

  implicit val adEntityUpdateFormat = jsonFormat6(ADEntityUpdate.apply)
}