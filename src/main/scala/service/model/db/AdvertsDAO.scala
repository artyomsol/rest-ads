package service.model.db

import service.utils.db._

/**
 * Project: rest-ads
 * Package: service.model.db
 * Created by asoloviov on 6/28/17 7:04 PM.
 */
class AdvertsDAO(implicit dBContext: DBContext) extends IndexDAO("accounts") {

}

object AdvertsDAO {
  def apply()(implicit dBContext: DBContext) = new AdvertsDAO()
}