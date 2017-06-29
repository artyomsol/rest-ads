package service.model.db

import org.scalatest.concurrent.Eventually
import org.scalatest.time.{Seconds, Span}
import org.scalatest.{Matchers, WordSpec}
import service.model.ADEntity
import service.rest.JsonSupport
import service.utils.{TestDBContext, TestData}

import scala.concurrent.duration._

/**
 * Project: rest-ads
 * Package: service.model.db
 * Created by asoloviov on 6/29/17 1:11 PM.
 */
class AdvertsDAOTest extends WordSpec with TestDBContext with Eventually with Matchers with TestData with JsonSupport {
  implicit val duration: FiniteDuration = 10.seconds
  implicit override val patienceConfig = PatienceConfig(timeout = scaled(Span(5, Seconds)))

  import service.model.ADEntity._
  import spray.json._

  import scala.concurrent.ExecutionContext.Implicits.global

  trait Context extends DBContextContext {
    val advertsDAO = dbContext.advertsDAO
    val indexName = advertsDAO.indexName
  }

  "AdvertsDAO" should {
    "use configured ES client" in new Context {
      advertsDAO.client should be theSameInstanceAs client
    }
    "initialize index with mappings" in new Context {
      advertsDAO.checkOrCreateIndex().await shouldEqual true
    }
    "store AD entity to ES " in new Context {
      val result = advertsDAO.create(oldCarAD).await
      result shouldEqual oldCarAD
    }
    "retrieve stored entity" in new Context {
      advertsDAO.create(oldCarAD).await shouldEqual oldCarAD
      advertsDAO.getByID(oldCarAD.id.get).await shouldEqual Some(oldCarAD)
    }
    "storing AD generates id of the new entity" in new Context {
      val result = advertsDAO.create(oldCarAD.copy(id = None)).await
      result shouldEqual oldCarAD.withID(result.id.get)
    }
    "delete stored entity" in new Context {
      advertsDAO.create(newCarAD).await shouldEqual newCarAD
      advertsDAO.deleteByID(newCarAD.id.get).await shouldEqual true
    }
    "override stored AD with direct create call " in new Context {
      val id = oldCarAD.id.get
      advertsDAO.create(oldCarAD).await shouldEqual oldCarAD
      advertsDAO.create(newCarAD).await shouldEqual newCarAD
      advertsDAO.client.execute(search(indexName) query idsQuery(id)).await.hits(0).sourceAsString.parseJson.convertTo[ADEntity] shouldEqual newCarAD
    }
    "update stored entity" in new Context {
      advertsDAO.create(newCarAD).await shouldEqual newCarAD
      advertsDAO.updateByID(newCarAD.id.get, updateToOldCarAD).await shouldEqual Some(oldCarAD)
      advertsDAO.getByID(newCarAD.id.get).await shouldEqual Some(oldCarAD)
    }
  }
}
