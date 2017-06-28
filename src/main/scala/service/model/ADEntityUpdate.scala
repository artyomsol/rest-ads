package service.model

import org.joda.time.DateTime
import service.model.FuelType._

import scala.util.Try

/**
 * Project: rest-ads
 * Package: service.model
 * Created by asoloviov on 6/27/17 11:12 PM.
 */
case class ADEntityUpdate(title: Option[String], fuel: Option[FuelType], price: Option[Int], `new`: Option[Boolean], mileage: Option[Int], firstRegistration: Option[DateTime] = None) {
  //TODO must be tested
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

  def toADEntityWithID(id: Long): Try[ADEntity] = Try(
    ADEntity(id,
      title.ensuring(_.isDefined, "title.field.empty").get,
      fuel.ensuring(_.isDefined, "fuel.field.empty").get,
      price.ensuring(_.isDefined, "price.field.empty").get,
      `new`.ensuring(_.isDefined, "new.field.empty").get,
      mileage, firstRegistration
    )
  )
}

object ADEntityUpdate {

  import service.utils.JodaDateTimeExtensions._
  import spray.json.DefaultJsonProtocol._

  implicit val adEntityUpdateFormat = jsonFormat6(ADEntityUpdate.apply)
}