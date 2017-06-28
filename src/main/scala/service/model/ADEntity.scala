package service.model

import java.util.UUID

import org.joda.time.DateTime
import service.model.ADEntity.IDType
import service.model.FuelType._
import service.utils.JodaDateTimeExtensions

/**
 * Project: rest-ads
 * Package: service.model
 * Created by asoloviov on 6/27/17 3:07 PM.
 */
case class ADEntity(id: Option[IDType], title: String, fuel: FuelType, price: Int, `new`: Boolean, mileage: Option[Int] = None, `first registration`: Option[DateTime] = None) {
  require(!title.isEmpty, "title.field.empty")
  require(`new` == mileage.isEmpty, "new.car.mileage.defined")
  require(`new` == `first registration`.isEmpty, "new.car.firstRegistration.defined")

  def withID(thatID: IDType): ADEntity = copy(id = Some(thatID))

  def idDefined: Boolean = id.isDefined
}

object ADEntity {
  type IDType = String

  import JodaDateTimeExtensions._
  import spray.json.DefaultJsonProtocol._

  implicit class entityAD2UpdateAD(val ad: ADEntity) extends AnyVal {
    def toUpdateAD: ADEntityUpdate = ADEntityUpdate(Some(ad.title), Some(ad.fuel), Some(ad.price), Some(ad.`new`), ad.mileage, ad.`first registration`)
  }

  implicit val adEntityFormat = jsonFormat7(ADEntity.apply)

  def getNextID: IDType = UUID.randomUUID().toString.filterNot(_ == "-")
}
