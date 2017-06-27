package service.rest

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.testkit.ScalatestRouteTest
import org.mockito.Mockito.when
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{Matchers, WordSpec}
import service.rest.routes.ADServiceRoute
import service.services.ADService

import scala.util.Random

/**
 * Project: rest-ads
 * Package: service.rest
 * Created by asoloviov on 6/27/17 6:51 PM.
 */
class RoutedHttpServiceTest extends WordSpec with Matchers with ScalatestRouteTest with MockitoSugar {

  trait Context {
    val adService = mock[ADService]
    val echoServiceRouter = mock[ADServiceRoute]
    when(echoServiceRouter.route).thenReturn(pathPrefix(Segment) { p => complete(p) })

    val httpService = new RoutedHttpService {
      val adServiceRouter = echoServiceRouter
    }
    val route = httpService.routes
    val randomPath = Random.alphanumeric.take(10).mkString
  }

  "HttpService" should {
    val GoodVersion = "good_version"
    "check api version of underlying service" in new Context {
      Get(s"/v1/$randomPath") ~> route ~> check {
        responseAs[String] shouldBe randomPath
      }
    }
    "return a error for requests to the wrong api version url" in new Context {
      Get(s"/$randomPath/") ~> Route.seal(route) ~> check {
        status shouldEqual StatusCodes.NotFound
      }
    }
  }
}
