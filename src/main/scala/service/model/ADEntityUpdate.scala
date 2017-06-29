package service.model

import org.joda.time.DateTime
import service.model.ADEntity.IDType
import service.model.FuelType._

import scala.util.Try

/**
 * Project: rest-ads
 * Package: service.model
 * Created by asoloviov on 6/27/17 11:12 PM.
 */
case class ADEntityUpdate(title: Option[String] = None, fuel: Option[FuelType] = None, price: Option[Int] = None, `new`: Option[Boolean]= None, mileage: Option[Int]= None, `first registration`: Option[DateTime] = None) {
  require(title.forall(_.nonEmpty), "title.field.empty")

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
      `first registration`.orElse(ad.`first registration`).filterNot(_ => resultingUsage)
    )
  }

  def toADEntityWithID(id: IDType): Try[ADEntity] = Try {
    require(title.isDefined, "title.field.empty")
    require(fuel.isDefined, "fuel.field.empty")
    require(price.isDefined, "price.field.empty")
    require(`new`.isDefined, "new.field.empty")
    require(`new`.isDefined, "new.field.empty")
    ADEntity(Some(id), title.get, fuel.get, price.get, `new`.get, mileage, `first registration`)
  }
}

object ADEntityUpdate {

  import service.utils.JodaDateTimeExtensions._
  import spray.json.DefaultJsonProtocol._

  implicit val adEntityUpdateFormat = jsonFormat6(ADEntityUpdate.apply)
}