package service.services

import akka.NotUsed
import akka.stream.scaladsl.Source
import service.model.{ADEntity, ADEntityUpdate}

import scala.concurrent.{ExecutionContext, Future}

/**
 * Project: rest-ads
 * Package: service.services
 * Created by asoloviov on 6/27/17 6:56 PM.
 */
class ADService(implicit ec: ExecutionContext) {
  def getAllADs(sortByField: String, desc: Boolean = false): Source[ADEntity, NotUsed] = ???

  def getADByID(id: Long): Future[ADEntity] = ???

  def createAD(ad: ADEntity): Future[ADEntity] = ???

  def updateAD(id: Long, adEntityUpdate: ADEntityUpdate): Future[Option[ADEntity]] = ???

  def deleteAD(id: Long): Future[Boolean] = ???

}
