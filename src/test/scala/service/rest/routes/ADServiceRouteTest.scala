package service.rest.routes

import akka.http.scaladsl.model.{HttpEntity, MediaTypes, Uri}
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.testkit.ScalatestRouteTest
import org.mockito.Mockito.{verify, when}
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{Matchers, WordSpec}
import service.model.ADEntity
import service.rest.JsonSupport
import service.services.ADService
import service.utils.TestData

import scala.concurrent.Future
import scala.util.Random

/**
 * Project: rest-ads
 * Package: service.rest.routes
 * Created by asoloviov on 6/27/17 9:45 PM.
 */
class ADServiceRouteTest extends WordSpec with Matchers with ScalatestRouteTest with MockitoSugar with JsonSupport with TestData {

  import service.model.ADEntity._
  import service.model.ADEntityUpdate._
  import spray.json._

  trait Context {
    val mockedADService = mock[ADService]
    val serviceRouter = new ADServiceRoute(mockedADService)
    val route = Route.seal(serviceRouter.route)
    val randomId = math.abs(Random.nextLong())
  }

  "ADServiceRoute" should {
    "accept GET /adverts/id requests" in new Context {
      when(mockedADService.getADByID(randomId)).thenReturn(Future.successful(oldCarAD.copy(id = randomId)))
      Get(s"/adverts/$randomId") ~> route ~> check {
        handled shouldBe true
        responseAs[ADEntity] shouldBe a[ADEntity]
        verify(mockedADService).getADByID(randomId)
      }
    }
    "accept POST /adverts/id requests" in new Context {
      val httpEntity = HttpEntity(MediaTypes.`application/json`, updateAD.toJson.compactPrint)
      when(mockedADService.updateAD(randomId, updateAD)).thenReturn(Future.successful(Some(oldCarAD.copy(id = randomId))))
      Post(Uri(s"/adverts/$randomId"), httpEntity) ~> route ~> check {
        handled shouldBe true
        responseAs[ADEntity] shouldBe a[ADEntity]
        verify(mockedADService).updateAD(randomId, updateAD)
      }
    }
  }
}
