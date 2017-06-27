package service.rest.routes

import akka.http.scaladsl.model.{HttpEntity, MediaTypes}
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.testkit.ScalatestRouteTest
import org.joda.time.DateTime
import org.mockito.Mockito.{verify, when}
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{Matchers, WordSpec}
import service.model.ADEntity._
import service.model.ADEntityUpdate._
import service.model.{ADEntity, ADEntityUpdate, FuelType}
import service.rest.JsonSupport
import service.services.ADService
import service.utils.TestData
import spray.json._

import scala.concurrent.Future
import scala.util.Random

/**
 * Project: rest-ads
 * Package: service.rest.routes
 * Created by asoloviov on 6/27/17 9:45 PM.
 */
class ADServiceRouteTest extends WordSpec with Matchers with ScalatestRouteTest with MockitoSugar with JsonSupport with TestData {


  trait Context {
    val mockedADService = mock[ADService]
    val serviceRouter = new ADServiceRoute(mockedADService)
    val route = Route.seal(serviceRouter.route)
    val randomId = Random.nextLong()
  }

  "ADServiceRoute" should {
    "accept GET /adverts/id requests" in new Context {
      when(mockedADService.getADByID(randomId)).thenReturn(Future.successful(oldCarAD.copy(id = randomId)))
      Get(s"/adverts/$randomId") ~> route ~> check {
        handled shouldBe true
        verify(mockedADService).getADByID(randomId)
        responseAs[ADEntity] shouldBe a[ADEntity]
      }
    }
    "accept POST /adverts/id requests" in new Context {
      val updateAD = ADEntityUpdate(Some(Random.nextString(10)), Some(FuelType.diesel), Some(10000), `new` = Some(false), mileage = Some(10001), Some(DateTime.now()))
      val httpEntity = HttpEntity(MediaTypes.`application/json`, updateAD.toJson.compactPrint)
      when(mockedADService.updateAD(randomId, updateAD)).thenReturn(Future.successful(Some(oldCarAD.copy(id = randomId))))
      Post(s"/adverts/$randomId", httpEntity) ~> route ~> check {
        handled shouldBe true
        verify(mockedADService).updateAD(randomId, updateAD)
        responseAs[ADEntity] shouldBe a[ADEntity]
      }
    }
  }
}
