package service.utils.db

import com.sksamuel.elastic4s.ElasticDsl._
import com.sksamuel.elastic4s.mappings.MappingDefinition
import org.joda.time.format.DateTimeFormatter
import service.utils.JodaDateTimeExtensions

import scala.concurrent.{ExecutionContext, Future}


/**
 * Project: rest-ads
 * Package: service.utils.db
 * Created by asoloviov on 6/28/17 7:45 PM.
 */
abstract class IndexDAO(forIndex: String)(implicit dBContext: DBContext) {
  val client = dBContext.getESClient
  val indexName = dBContext.indexPrefix + forIndex
  val timeFormat: DateTimeFormatter = JodaDateTimeExtensions.format
  val timePattern: String = JodaDateTimeExtensions.pattern
  val theMapping: MappingDefinition

  def checkOrCreateIndex()(implicit ec: ExecutionContext): Future[Boolean] = {
    client.execute(indexExists(indexName)).flatMap(existsResponse =>
      if (existsResponse.isExists) Future.successful(true)
      else client.execute(createIndex(indexName).mappings(theMapping)).map(_.isAcknowledged)
    )
  }
}
