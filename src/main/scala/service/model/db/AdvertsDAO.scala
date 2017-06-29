package service.model.db

import akka.NotUsed
import akka.actor.ActorSystem
import akka.stream.scaladsl.Source
import com.sksamuel.elastic4s.ElasticDsl._
import com.sksamuel.elastic4s.mappings.MappingDefinition
import com.sksamuel.elastic4s.mappings.dynamictemplate.DynamicMapping
import com.sksamuel.elastic4s.searches.RichSearchHit
import com.sksamuel.elastic4s.streams.ReactiveElastic._
import org.elasticsearch.action.DocWriteResponse.Result
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
class AdvertsDAO(implicit dBContext: DBContext) extends IndexDAO("adverts") {

  private val typeName = "ADEntity"
  override val theMapping: MappingDefinition = mapping(typeName).dynamic(DynamicMapping.Strict).all(false)
    .fields(
      keywordField("id"),
      keywordField("title"),
      keywordField("fuel"),
      intField("price"),
      booleanField("new"),
      intField("mileage"),
      dateField("first registration").format(timePattern)
    )

  import ADEntity._
  import spray.json._

  def create(ad: ADEntity)(implicit ec: ExecutionContext): Future[ADEntity] = {
    val adToStore = if (ad.idDefined) ad else ad.withID(ADEntity.getNextID)
    client.execute(indexInto(indexName / typeName) id adToStore.id.get doc adToStore.toJson.compactPrint)
      .map(indexResult => adToStore)
  }


  def getByID(id: IDType)(implicit ec: ExecutionContext): Future[Option[ADEntity]] =
    client.execute(search(indexName / typeName) query idsQuery(id))
      .map { resporse =>
        if (resporse.isEmpty) None
        else {
          val entity = resporse.hits(0).sourceAsString.parseJson.convertTo[ADEntity]
          Some(entity)
        }
      }

  def deleteByID(id: IDType)(implicit ec: ExecutionContext): Future[Boolean] = client.execute(delete(id) from indexName / typeName).map(_.getResult == Result.DELETED)

  def updateByID(id: IDType, adEntityUpdate: ADEntityUpdate)(implicit ec: ExecutionContext): Future[Option[ADEntity]] =
  // in order to check field restrictions we must merge adverts manually instead of ES update query using
  // TODO implement document version aware update with retries on conflicts
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
    val query = search(indexName / typeName) sortBy (fieldSort(sortByField) order (if (desc) SortOrder.DESC else SortOrder.ASC))
    val publisher = client.publisher(query)
    Source.fromPublisher(publisher).mapAsync(parallelism)(hitToEntity)
  }
}

object AdvertsDAO {
  def apply()(implicit dBContext: DBContext) = new AdvertsDAO()
}