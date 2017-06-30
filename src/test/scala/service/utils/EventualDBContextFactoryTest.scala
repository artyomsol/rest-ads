package service.utils

import akka.actor.ActorSystem
import org.scalatest.Matchers
import service.utils.db.{DBContextActor, DBContext}

import scala.concurrent.Future
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
    "create DBContext when Elasticsearch available" in new TestContext{
      class TestEventualDBContextFactory(timeout: FiniteDuration)(implicit system: ActorSystem, appConfig: AppConfig) extends EventualDBContextFactory(timeout) {
        override def dbContextActorProps = DBContextActor.props(Future.successful(dbContext))
      }
      val factory = new EventualDBContextFactory(5.seconds)
      factory.getDBContext.await should be theSameInstanceAs factory.getDBContext.await
    }

    "fail DBContext creation after configured timeout" in {
    }

    "create DBContext when Elasticsearch comes online eventually within configured timeout" in {
    }
  }

}
