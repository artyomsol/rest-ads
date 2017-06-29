package service.utils

import org.scalatest.concurrent.Eventually
import org.scalatest.time.{Seconds, Span}
import service.services.ADService

import scala.concurrent.duration._

/**
 * Project: rest-ads
 * Package: service.utils
 * Created by asoloviov on 6/29/17 11:32 PM.
 */
class ServiceSpec extends AkkaSystemSpec with TestDBContext with TestData with Eventually {
  implicit val duration: FiniteDuration = 10.seconds
  implicit override val patienceConfig = PatienceConfig(timeout = scaled(Span(5, Seconds)))

  trait Context extends SystemContext with DBContextContext {
    val testAdvertsNumber: Int = 10
    val testAdverts = generateAdverts(testAdvertsNumber)
    val adService = new ADService()(system, dbContext)
    dbContext.init(duration)
    blockUntilIndexExists(dbContext.advertsDAO.indexName)
    testAdverts.foreach(dbContext.advertsDAO.create)
    blockUntilCount(testAdvertsNumber, dbContext.advertsDAO.indexName)
  }

}
