package service.model.db

import service.model.ADEntity
import service.utils.db._

import scala.concurrent.Future

/**
 * Project: rest-ads
 * Package: service.model.db
 * Created by asoloviov on 6/28/17 7:04 PM.
 */
class AdvertsDAO(implicit dBContext: DBContext) extends IndexDAO("accounts") {
  def getByID(id: String): Future[ADEntity] = ???
}

object AdvertsDAO {
  def apply()(implicit dBContext: DBContext) = new AdvertsDAO()
}