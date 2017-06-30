package service.rest

import akka.http.scaladsl.model.{HttpEntity, MediaTypes, StatusCodes}
import org.scalatest.Matchers
import service.model.ADEntity
import service.services.ADService
import service.utils.ServiceSpec

import scala.concurrent.Future

/**
 * Project: rest-ads
 * Package: service.rest
 * Created by asoloviov on 6/29/17 11:19 PM.
 */
class HttpServiceTest extends ServiceSpec with Matchers with JsonSupport {

  import ADEntity._

  trait TestContext extends Context {
    override val adService = new ADService(Future.successful(dbContext))(system)
    val handler = new HttpService(adService)(executor, appConfig)
    val route = handler.route
  }

  "HttpServiceTest" should {

    "accept adverts via REST API" in new TestContext {
      val id = "test_id"
      val httpEntity = HttpEntity(MediaTypes.`application/json`, newCarADJson)
      val expectedAD = newCarAD.withID(id)
      Post(s"/v1/adverts/$id", httpEntity) ~> route ~> check {
        status shouldEqual StatusCodes.OK
        responseAs[ADEntity] shouldEqual expectedAD
      }
      Get(s"/v1/adverts/$id") ~> route ~> check {
        status shouldEqual StatusCodes.OK
        responseAs[ADEntity] shouldEqual expectedAD
      }
    }

    "retrieve stored advert " in new TestContext {
      val expectedAD = testAdverts(9)
      val id = expectedAD.id.get
      Get(s"/v1/adverts/$id") ~> route ~> check {
        status shouldEqual StatusCodes.OK
        responseAs[ADEntity] shouldEqual expectedAD
      }
    }

    "report error on retrieve of unknown document id" in new TestContext {
      val id = ADEntity.getNextID
      Get(s"/v1/adverts/$id") ~> route ~> check {
        status shouldEqual StatusCodes.NotFound
      }
    }

    "delete advert via REST API" in new TestContext {
      val id = testAdverts(6).id.get
        Delete(s"/v1/adverts/$id") ~> route ~> check(status shouldEqual StatusCodes.NoContent)
        Delete(s"/v1/adverts/${id + "_"}") ~> route ~> check(status shouldEqual StatusCodes.NotFound)
    }

  }
}
