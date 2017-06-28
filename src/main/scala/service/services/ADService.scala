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

  private def createNewAD(ad: ADEntityUpdate): Future[ADEntity] = ???

  def createAD(ad: ADEntityUpdate, idOpt: Option[Long]): Future[ADEntity] =
    idOpt.fold(createNewAD(ad))(id => createOrUpdateAD(id, ad))

  import ADEntity._

  def createAD(ad: ADEntity): Future[ADEntity] = createAD(ad.toUpdateAD, Some(ad.id))

  def createOrUpdateAD(id: Long, adEntityUpdate: ADEntityUpdate): Future[ADEntity] = ???

  def deleteAD(id: Long): Future[Boolean] = ???

}
