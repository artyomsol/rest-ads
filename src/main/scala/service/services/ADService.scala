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

  private def throwDataNotFound(id: IDType): Nothing = throw new DataNotFoundException(s"document id=$id not found")

  private def checkEmptyResponse(id: IDType) = (response: Option[ADEntity]) => response.fold[ADEntity](throwDataNotFound(id))(identity)

  private val fieldsSet = classOf[ADEntity].getDeclaredFields.map(_.getName)
  def getAllADs(sortByField: String = "id", desc: Boolean = false): Source[ADEntity, NotUsed] = {
    require(fieldsSet.contains(sortByField), s"field.not.exists")
    getAllPublisher(sortByField, desc)
  }

  def getADByID(id: IDType): Future[ADEntity] = getByID(id).map(checkEmptyResponse(id))

  def createAD(ad: ADEntity): Future[ADEntity] = create(ad)

  def updateAD(id: IDType, adEntityUpdate: ADEntityUpdate): Future[ADEntity] = updateByID(id, adEntityUpdate).map(checkEmptyResponse(id))

  def deleteAD(id: IDType): Future[Boolean] = deleteByID(id).map(deleted => if (deleted) true else throwDataNotFound(id))

}

class DataNotFoundException(message: String) extends Throwable