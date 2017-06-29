package service.services

import org.scalatest.Matchers
import org.scalatest.concurrent.Eventually
import org.scalatest.time.{Seconds, Span}
import service.utils.{ServiceSpec, TestDBContext, TestData}

import scala.concurrent.duration._

/**
 * Project: rest-ads
 * Package: service.services
 * Created by asoloviov on 6/29/17 4:55 PM.
 */
class ADServiceTest extends ServiceSpec with TestDBContext with Eventually with Matchers with TestData {
  implicit val duration: FiniteDuration = 10.seconds
  implicit override val patienceConfig = PatienceConfig(timeout = scaled(Span(5, Seconds)))

  trait Context extends SystemContext with DBContextContext {
    val testAdverts = generateAdverts(10)
    val adService = new ADService()(system, dbContext)
  }

  "ADServiceTest" should {

    "updateAD" in {

    }

    "getADByID" in {

    }

    "getAllADs" in {

    }

    "getAllADs$default$2" in {

    }

    "deleteAD" in {

    }

    "createAD" in {

    }

  }
}
