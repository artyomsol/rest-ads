package service.rest.routes

import akka.http.scaladsl.model.{HttpEntity, MediaTypes, StatusCodes, Uri}
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

    def generateId = math.abs(Random.nextLong())

    val randomId = generateId
    val entityEndpoint: Uri = Uri(s"/adverts/$randomId", Uri.ParsingMode.Strict)
  }

  "ADServiceRoute" should {
    "accept POST /adverts/ requests to add car advert with id specified" in new Context {
      val entity = HttpEntity(MediaTypes.`application/json`, oldCarAD.toJson.compactPrint)
      when(mockedADService.createAD(updateToOldCarAD, Some(oldCarAD.id))).thenReturn(Future.successful(oldCarAD))
      when(mockedADService.createAD(oldCarAD)).thenCallRealMethod()
      Post("/adverts/", entity) ~> route ~> check {
        handled shouldBe true
        responseAs[ADEntity] shouldBe a[ADEntity]
        verify(mockedADService).createAD(updateToOldCarAD, Some(oldCarAD.id))
      }
    }
    "accept POST /adverts/id requests to add car advert with id specified" in new Context {
      // id specified in the URI path has higher priority then id from the JSON object in body
      val entity = HttpEntity(MediaTypes.`application/json`, oldCarAD.toJson.compactPrint)
      val expectedAD = oldCarAD.copy(id = randomId)
      when(mockedADService.createOrUpdateAD(randomId, updateToOldCarAD)).thenReturn(Future.successful(expectedAD))
      Post(entityEndpoint, entity) ~> route ~> check {
        handled shouldBe true
        responseAs[ADEntity] shouldBe a[ADEntity]
        verify(mockedADService).createOrUpdateAD(randomId, updateToOldCarAD)
      }
    }
    "accept GET /adverts/id requests to return data for single car advert by id" in new Context {
      when(mockedADService.getADByID(randomId)).thenReturn(Future.successful(oldCarAD.copy(id = randomId)))
      Get(entityEndpoint) ~> route ~> check {
        handled shouldBe true
        responseAs[ADEntity] shouldBe a[ADEntity]
        verify(mockedADService).getADByID(randomId)
      }
    }
    "accept POST /adverts/id requests to modify car advert by id" in new Context {
      val httpEntity = HttpEntity(MediaTypes.`application/json`, updateAD.toJson.compactPrint)
      when(mockedADService.createOrUpdateAD(randomId, updateAD)).thenReturn(Future.successful(oldCarAD.copy(id = randomId)))
      Post(entityEndpoint, httpEntity) ~> route ~> check {
        handled shouldBe true
        responseAs[ADEntity] shouldBe a[ADEntity]
        verify(mockedADService).createOrUpdateAD(randomId, updateAD)
      }
    }
    "accept DELETE /adverts/id requests to delete car advert by id" in new Context {
      when(mockedADService.deleteAD(randomId)).thenReturn(Future.successful(true))
      Delete(entityEndpoint) ~> route ~> check {
        handled shouldBe true
        verify(mockedADService).deleteAD(randomId)
      }
    }
  }
  it when {
    "processing Delete request" should {
      "result with NoContent status code on success" in new Context {
        when(mockedADService.deleteAD(randomId)).thenReturn(Future.successful(true))
        Delete(entityEndpoint) ~> route ~> check {
          status shouldBe StatusCodes.NoContent
        }
      }
      "result with NotFound status code if id was not found" in new Context {
        when(mockedADService.deleteAD(randomId)).thenReturn(Future.successful(false))
        Delete(entityEndpoint) ~> route ~> check {
          status shouldBe StatusCodes.NotFound
        }
      }
    }
  }
}
