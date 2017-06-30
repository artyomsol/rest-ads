package service.utils

import org.scalatest.Matchers

import scala.concurrent.duration._

/**
 * Project: rest-ads
 * Package: service.utils
 * Created by asoloviov on 6/30/17 9:37 AM.
 */
class EventualDBContextFactoryTest extends ServiceSpec with TestDBContext with Matchers {

  trait TestContext extends DBContextContext with SystemContext {

  }

  "EventualDBContextFactory" should {
    "be initialized normally" in new TestContext {
      new EventualDBContextFactory(5.seconds) shouldBe a[EventualDBContextFactory]
    }
    //TODO tests to develop
    "create DBContext when Elasticsearch available" in {
    }

    "fail DBContext creation after configured timeout" in {
    }

    "create DBContext when Elasticsearch comes online eventually within configured timeout" in {
    }
  }

}
