package service.rest

import akka.http.scaladsl.common.{EntityStreamingSupport, JsonEntityStreamingSupport}
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json.DefaultJsonProtocol

/**
 * Project: rest-ads
 * Package: service.rest
 * Created by asoloviov on 6/27/17 10:25 PM.
 */
trait JsonSupport extends SprayJsonSupport with DefaultJsonProtocol {
  implicit val jsonStreamingSupport: JsonEntityStreamingSupport = EntityStreamingSupport.json()
}
