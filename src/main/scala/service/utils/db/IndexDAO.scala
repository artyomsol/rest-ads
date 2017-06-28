package service.utils.db


/**
 * Project: rest-ads
 * Package: service.utils.db
 * Created by asoloviov on 6/28/17 7:45 PM.
 */
class IndexDAO(forIndex: String) (implicit dBContext: DBContext) {
  val client = dBContext.getESClient
  val indexName = dBContext.indexPrefix + forIndex
}
