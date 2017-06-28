package service.model.db

import akka.NotUsed
import akka.actor.ActorSystem
import akka.stream.scaladsl.Source
import com.sksamuel.elastic4s.ElasticDsl._
import com.sksamuel.elastic4s.RichSearchHit
import com.sksamuel.elastic4s.mappings.FieldType.{StringType, _}
import com.sksamuel.elastic4s.mappings.{DynamicMapping, MappingDefinition}
import com.sksamuel.elastic4s.streams.ReactiveElastic._
import service.model.ADEntity.IDType
import service.model.{ADEntity, ADEntityUpdate}
import service.utils.db._

import scala.concurrent.{ExecutionContext, Future}

/**
 * Project: rest-ads
 * Package: service.model.db
 * Created by asoloviov on 6/28/17 7:04 PM.
 */
class AdvertsDAO(implicit dBContext: DBContext) extends IndexDAO("accounts") {
  override val theMapping: MappingDefinition = mapping("ADEntity").dynamic(DynamicMapping.Strict).all(false).fields(
    "id".typed(StringType).analyzer(NotAnalyzed).docValuesFormat(true),
    "title".typed(StringType).analyzer(NotAnalyzed).docValuesFormat(true),
    "fuel".typed(StringType).analyzer(NotAnalyzed).docValuesFormat(true),
    "price".typed(IntegerType),
    "new".typed(BooleanType),
    "mileage".typed(IntegerType),
    "first registration".typed(DateType).format(timePattern)
  )

  def create(ad: ADEntity)(implicit ec: ExecutionContext): Future[ADEntity] = ???

  def getByID(id: IDType)(implicit ec: ExecutionContext): Future[ADEntity] = ???

  def deleteByID(id: IDType)(implicit ec: ExecutionContext): Future[Boolean] = ???

  def updateByID(id: IDType, adEntityUpdate: ADEntityUpdate)(implicit ec: ExecutionContext): Future[Option[ADEntity]] = ???

  private def hitToEntity(hit: RichSearchHit)(implicit ec: ExecutionContext): Future[ADEntity] = Future {
    import ADEntity._
    import spray.json._
    hit.sourceAsString.parseJson.convertTo[ADEntity]
  }

  private val parallelism = Runtime.getRuntime.availableProcessors()

  def getAllPublisher(sortByField: String, desc: Boolean)(implicit system: ActorSystem): Source[ADEntity, NotUsed] = {
    import system.dispatcher
    val query = ???
    val publisher = client.publisher(query)
    Source.fromPublisher(publisher).mapAsync(parallelism)(hitToEntity)
  }
}

object AdvertsDAO {
  def apply()(implicit dBContext: DBContext) = new AdvertsDAO()
}