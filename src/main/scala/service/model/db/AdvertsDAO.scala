package service.model.db

import akka.NotUsed
import akka.actor.ActorSystem
import akka.stream.scaladsl.Source
import com.sksamuel.elastic4s.ElasticDsl._
import com.sksamuel.elastic4s.RichSearchHit
import com.sksamuel.elastic4s.mappings.FieldType.{StringType, _}
import com.sksamuel.elastic4s.mappings.{DynamicMapping, IdField, MappingDefinition}
import com.sksamuel.elastic4s.streams.ReactiveElastic._
import org.elasticsearch.search.sort.SortOrder
import service.model.{ADEntity, ADEntityUpdate}
import service.utils.db._

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Success

/**
 * Project: rest-ads
 * Package: service.model.db
 * Created by asoloviov on 6/28/17 7:04 PM.
 */
class AdvertsDAO(implicit dBContext: DBContext) extends IndexDAO("accounts") {
  override val theMapping: MappingDefinition = mapping("ADEntity").dynamic(DynamicMapping.Strict).all(false)
    .id(IdField("id"))
    .fields(
      field("id").typed(StringType).analyzer(NotAnalyzed).docValuesFormat(true),
      field("title").typed(StringType).analyzer(NotAnalyzed).docValuesFormat(true),
      field("fuel").typed(StringType).analyzer(NotAnalyzed).docValuesFormat(true),
      field("price").typed(IntegerType),
      field("new").typed(BooleanType),
      field("mileage").typed(IntegerType),
      field("first registration").typed(DateType).format(timePattern)
    )

  import ADEntity._
  import spray.json._

  def create(ad: ADEntity)(implicit ec: ExecutionContext): Future[ADEntity] = {
    val adToStore = if (ad.idDefined) ad else ad.withID(ADEntity.getNextID)
    client.execute(index into indexName -> "ADEntity" source adToStore.toJson.compactPrint).map(indexResult => adToStore)
  }


  def getByID(id: IDType)(implicit ec: ExecutionContext): Future[Option[ADEntity]] = client.execute(search in indexName -> "ADEntity" size 1 query termQuery("id" -> id))
    .map { resporse =>
      if (resporse.totalHits == 0) None
      else {
        val entity = resporse.hits(0).getSourceAsString.parseJson.convertTo[ADEntity]
        Some(entity)
      }
    }

  def deleteByID(id: IDType)(implicit ec: ExecutionContext): Future[Boolean] = client.execute(delete(id) from indexName -> "ADEntity").map(_.isFound)

  def updateByID(id: IDType, adEntityUpdate: ADEntityUpdate)(implicit ec: ExecutionContext): Future[Option[ADEntity]] =
  // in order to check field restrictions we must merge adverts manually instead of ES update query using
    getByID(id).flatMap {
      case Some(ad) => create(adEntityUpdate.applyTo(ad)).map(Some.apply)
      case None => adEntityUpdate.toADEntityWithID(id) match {
        case Success(newAD) => create(newAD).map(Some.apply)
        case _ => Future.successful(None)
      }
    }

  private def hitToEntity(hit: RichSearchHit)(implicit ec: ExecutionContext): Future[ADEntity] = Future {
    hit.sourceAsString.parseJson.convertTo[ADEntity]
  }

  private val parallelism = Runtime.getRuntime.availableProcessors()

  def getAllPublisher(sortByField: String, desc: Boolean)(implicit system: ActorSystem): Source[ADEntity, NotUsed] = {
    import system.dispatcher
    val query = search in indexName -> "ADEntity" sort (field sort sortByField order (if (desc) SortOrder.DESC else SortOrder.ASC))
    val publisher = client.publisher(query)
    Source.fromPublisher(publisher).mapAsync(parallelism)(hitToEntity)
  }
}

object AdvertsDAO {
  def apply()(implicit dBContext: DBContext) = new AdvertsDAO()
}