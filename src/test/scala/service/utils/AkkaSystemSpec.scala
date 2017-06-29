package service.utils

import akka.http.scaladsl.testkit.ScalatestRouteTest
import akka.testkit.TestKit
import org.scalatest.{BeforeAndAfterAll, WordSpec}

/**
 * Project: rest-ads
 * Package: service.utils
 * Created by asoloviov on 6/29/17 5:32 PM.
 */
class AkkaSystemSpec extends WordSpec with ScalatestRouteTest with BeforeAndAfterAll {

  trait SystemContext {
  }

  override def afterAll() {
    TestKit.shutdownActorSystem(system)
    super.afterAll()
  }
}
