package service.rest.routes

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import service.model.{ADEntity, ADEntityUpdate}
import service.rest.JsonSupport
import service.services.ADService

/**
 * Project: rest-ads
 * Package: service.rest.routes
 * Created by asoloviov on 6/27/17 8:51 PM.
 */
class ADServiceRoute(adService: ADService) extends JsonSupport {

  import ADEntity._
  import ADEntityUpdate._
  import adService._

  val route: Route = pathPrefix("adverts") {
    pathEndOrSingleSlash {
      get {
        complete(getAllADs("id"))
      } ~
      post {
        entity(as[ADEntity]) { adEntity=>
          complete(createAD(adEntity))
        }
      }
    } ~
      pathPrefix(LongNumber) { id =>
        pathEndOrSingleSlash {
          get {
            complete(getADByID(id))
          } ~
            post {
              entity(as[ADEntityUpdate]) { adEntityUpdate =>
                complete(createOrUpdateAD(id, adEntityUpdate))
              }
            } ~
            delete {
              onSuccess(deleteAD(id)) { success =>
                complete(
                  if (success) StatusCodes.NoContent else StatusCodes.NotFound
                )
              }
            }
        }
      }
  }
}
