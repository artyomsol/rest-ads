package service.services

import akka.NotUsed
import akka.actor.ActorSystem
import akka.stream.scaladsl.Source
import service.model.ADEntity.IDType
import service.model.db.AdvertsDAO
import service.model.{ADEntity, ADEntityUpdate}
import service.utils.DataNotFoundException
import service.utils.db.DBContext

import scala.concurrent.Future

/**
 * Project: rest-ads
 * Package: service.services
 * Created by asoloviov on 6/27/17 6:56 PM.
 */
class ADService(dBContextFuture: => Future[DBContext])(implicit system: ActorSystem) {

  import service.model.db.AdvertsDAO._
  import system.dispatcher

  private def throwDataNotFound(id: IDType): Nothing = throw DataNotFoundException(s"document id=$id not found")

  private def checkEmptyResponse(id: IDType) = (response: Option[ADEntity]) => response.fold[ADEntity](throwDataNotFound(id))(identity)

  private val fieldsSet = classOf[ADEntity].getDeclaredFields.map(_.getName)

  private def daoCall[T](method: AdvertsDAO => Future[T]) = dBContextFuture.flatMap(dbContext => method(dbContext.advertsDAO))

  def getAllADs(sortByField: String = "id", desc: Boolean = false): Future[Source[ADEntity, NotUsed]] = {
    require(fieldsSet.contains(sortByField), s"field.not.exists")
    daoCall(getAllPublisher(sortByField, desc))
  }

  def getADByID(id: IDType): Future[ADEntity] = daoCall(getByID(id)).map(checkEmptyResponse(id))

  def createAD(ad: ADEntity): Future[ADEntity] = daoCall(create(ad))

  def updateAD(id: IDType, adEntityUpdate: ADEntityUpdate): Future[ADEntity] = daoCall(updateByID(id, adEntityUpdate)).map(checkEmptyResponse(id))

  def deleteAD(id: IDType): Future[Boolean] = daoCall(deleteByID(id)).map(deleted => if (deleted) true else throwDataNotFound(id))

}

