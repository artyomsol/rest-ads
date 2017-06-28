package service.rest.routes

import akka.http.scaladsl.model.{HttpEntity, MediaTypes, StatusCodes, Uri}
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.testkit.ScalatestRouteTest
import org.mockito.ArgumentMatchers._
import org.mockito.Mockito.{verify, when}
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{Matchers, WordSpec}
import service.model.{ADEntity, ADEntityUpdate}
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
    "accept POST /adverts/ requests to add car advert with empty id field" in new Context {
      val emptyIDADEntity = oldCarAD.copy(id = None)
      val entity = HttpEntity(MediaTypes.`application/json`, emptyIDADEntity.toJson.compactPrint)
      when(mockedADService.createAD(emptyIDADEntity)).thenReturn(Future.successful(oldCarAD))
      Post("/adverts/", entity) ~> route ~> check {
        handled shouldBe true
        responseAs[ADEntity] shouldBe a[ADEntity]
        verify(mockedADService).createAD(emptyIDADEntity)
      }
    }
    "accept POST /adverts/ requests to add car advert with id specified" in new Context {
      val entity = HttpEntity(MediaTypes.`application/json`, oldCarAD.toJson.compactPrint)
      when(mockedADService.createAD(oldCarAD)).thenReturn(Future.successful(oldCarAD))
      Post("/adverts/", entity) ~> route ~> check {
        handled shouldBe true
        responseAs[ADEntity] shouldBe a[ADEntity]
        verify(mockedADService).createAD(oldCarAD)
      }
    }
    "accept POST /adverts/id requests to add car advert with id specified" in new Context {
      // id specified in the URI path has higher priority then id from the JSON object in body
      val entity = HttpEntity(MediaTypes.`application/json`, oldCarAD.toJson.compactPrint)
      val expectedAD = oldCarAD.withID(randomId)
      when(mockedADService.createAD(expectedAD)).thenReturn(Future.successful(expectedAD))
      Post(entityEndpoint, entity) ~> route ~> check {
        handled shouldBe true
        responseAs[ADEntity] shouldBe a[ADEntity]
        verify(mockedADService).createAD(expectedAD)
      }
    }
    "accept GET /adverts/id requests to return data for single car advert by id" in new Context {
      //TODO check response status in case if data was not found
      val expectedAD = oldCarAD.withID(randomId)
      when(mockedADService.getADByID(randomId)).thenReturn(Future.successful(expectedAD))
      Get(entityEndpoint) ~> route ~> check {
        handled shouldBe true
        responseAs[ADEntity] shouldEqual expectedAD
        verify(mockedADService).getADByID(randomId)
      }
    }
    "accept POST /adverts/id requests to modify car advert by id" in new Context {
      val httpEntity = HttpEntity(MediaTypes.`application/json`, updateAD.toJson.compactPrint)
      when(mockedADService.updateAD(randomId, updateAD)).thenReturn(Future.successful(Some(oldCarAD.withID(randomId))))
      Post(entityEndpoint, httpEntity) ~> route ~> check {
        handled shouldBe true
        responseAs[ADEntity] shouldBe a[ADEntity]
        verify(mockedADService).updateAD(randomId, updateAD)
      }
    }
    "accept POST /adverts/id requests with all fields filled and treat this as a create request" in new Context {
      val httpEntity = HttpEntity(MediaTypes.`application/json`, totalUpdateAD.toJson.compactPrint)
      val expectedResult = totalUpdateAD.toADEntityWithID(randomId).get
      when(mockedADService.createAD(expectedResult)).thenReturn(Future.successful(expectedResult))
      Post(entityEndpoint, httpEntity) ~> route ~> check {
        handled shouldBe true
        responseAs[ADEntity] shouldEqual expectedResult
        verify(mockedADService).createAD(expectedResult)
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
  it when {
    "creating/modifying advert" should {
      "validate required non empty title" in new Context {
        val invalidOldCarADJson = """{"title":"","mileage":100000,"price":100,"fuel":"diesel","id":10,"new":false,"firstRegistration":"2017-06-27T00:00:00.000+0000"}"""
        val entity = HttpEntity(MediaTypes.`application/json`, invalidOldCarADJson)
        Post(entityEndpoint, entity) ~> route ~> check {
          handled shouldBe true
          status shouldBe StatusCodes.BadRequest
          responseAs[String] should include regex """requirement failed: title\.field\.empty"""
        }
      }
      "validate required fuel type field if no id provided" in new Context {
        // absent id means we are supposed to create new record
        val invalidOldCarADJson = """{"title":"test","mileage":100000,"price":100,"id":10,"new":false,"firstRegistration":"2017-06-27T00:00:00.000+0000"}"""
        val entity = HttpEntity(MediaTypes.`application/json`, invalidOldCarADJson)
        Post("/adverts/", entity) ~> route ~> check {
          handled shouldBe true
          status shouldBe StatusCodes.BadRequest
          responseAs[String] should include regex """The request content was malformed:\sObject is missing required member 'fuel'"""
        }
      }
      "treat absent required field with id field specified in URI as update data request" in new Context {
        val invalidOldCarADJson = """{"title":"test","mileage":100000,"price":100,"id":10,"new":false,"firstRegistration":"2017-06-27T00:00:00.000+0000"}"""
        val updateADFromJson = invalidOldCarADJson.parseJson.convertTo[ADEntityUpdate]
        val entity = HttpEntity(MediaTypes.`application/json`, invalidOldCarADJson)
        val expectedAD = oldCarAD.withID(randomId)
        when(mockedADService.updateAD(any(classOf[Long]), any(classOf[ADEntityUpdate]))).thenReturn(Future(Some(expectedAD)))
        Post(entityEndpoint, entity) ~> route ~> check {
          handled shouldBe true
          status shouldBe StatusCodes.OK
          responseAs[ADEntity] shouldEqual expectedAD
        }
      }
    }
  }
}
