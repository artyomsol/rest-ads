package service.model.db

import com.sksamuel.elastic4s.testkit.{ElasticSugar, SearchMatchers}
import org.scalatest.concurrent.Eventually
import org.scalatest.time.{Seconds, Span}
import org.scalatest.{Matchers, WordSpec}
import service.utils.db.DBContext
import service.utils.{AppConfig, TestData}

import scala.concurrent.duration._

/**
 * Project: rest-ads
 * Package: service.model.db
 * Created by asoloviov on 6/29/17 1:11 PM.
 */
class AdvertsDAOTest extends WordSpec with ElasticSugar with Eventually with Matchers with SearchMatchers with TestData {
  implicit val duration: FiniteDuration = 10.seconds
  implicit override val patienceConfig = PatienceConfig(timeout = scaled(Span(5, Seconds)))
  import scala.concurrent.ExecutionContext.Implicits.global

  val appConfig = AppConfig()
  val dbContext = new DBContext(() => client,"test_", appConfig)
  val advertsDAO = dbContext.advertsDAO
  val indexName = advertsDAO.indexName
  "AdvertsDAO" should {
    "use configured ES client" in {
      advertsDAO.client should be theSameInstanceAs client
    }
    "initialize index with mappings" in {
      eventually(advertsDAO.checkOrCreateIndex()).await shouldEqual true
    }
    "store AD entity to ES " in {
      advertsDAO.create(oldCarAD).await shouldEqual oldCarAD
      blockUntilCount(1, indexName)
      succeed
    }
    "retrieve stored entity" in {
      advertsDAO.create(oldCarAD).await shouldEqual oldCarAD
      blockUntilCount(1, indexName)
      advertsDAO.getByID(oldCarAD.id.get).await shouldEqual Some(oldCarAD)
    }
  }
}
