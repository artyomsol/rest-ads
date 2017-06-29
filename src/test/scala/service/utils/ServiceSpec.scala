package service.utils

import akka.actor.ActorSystem
import akka.testkit.{ImplicitSender, TestKit}
import org.scalatest.{BeforeAndAfterAll, WordSpecLike}

/**
 * Project: rest-ads
 * Package: service.utils
 * Created by asoloviov on 6/29/17 5:32 PM.
 */
class ServiceSpec extends TestKit(ActorSystem("ServiceSpec")) with ImplicitSender with WordSpecLike with BeforeAndAfterAll {

  trait SystemContext {

  }
  override def afterAll() {
    TestKit.shutdownActorSystem(system)
    super.afterAll()
  }

}
