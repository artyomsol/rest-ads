package service.services

import akka.NotUsed
import akka.actor.ActorSystem
import akka.stream.scaladsl.Source
import service.model.ADEntity.IDType
import service.model.{ADEntity, ADEntityUpdate}
import service.utils.db.DBContext

import scala.concurrent.Future

/**
 * Project: rest-ads
 * Package: service.services
 * Created by asoloviov on 6/27/17 6:56 PM.
 */
class ADService(implicit system: ActorSystem, dBContext: DBContext) {
  import dBContext.advertsDAO._
  import system.dispatcher
  def getAllADs(sortByField: String, desc: Boolean = false): Source[ADEntity, NotUsed] = getAllPublisher(sortByField, desc)

  def getADByID(id: IDType): Future[ADEntity] = getByID(id)

  def createAD(ad: ADEntity): Future[ADEntity] = create(ad)

  def updateAD(id: IDType, adEntityUpdate: ADEntityUpdate): Future[Option[ADEntity]] =  updateByID(id, adEntityUpdate)

  def deleteAD(id: IDType): Future[Boolean] = deleteByID(id)

}
