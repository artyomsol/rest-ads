package service.utils

import akka.actor._
import akka.pattern.{Backoff, BackoffSupervisor, ask}
import service.Main._
import service.utils.db.DBContextActor.GetDBContext
import service.utils.db.{DBContext, DBContextActor}

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.util.Random

/**
 * Project: rest-ads
 * Package: service.utils
 * Created by asoloviov on 6/30/17 9:46 AM.
 */
class EventualDBContextFactory(val timeout: FiniteDuration)(implicit system: ActorSystem, appConfig: AppConfig) {
  def dbContextActorProps = DBContextActor.props(DBContext(cnf).init())

  private val dbContextActorName = "dbContext-" + Random.alphanumeric.take(5).mkString
  private val backoffSupervisorProps = BackoffSupervisor.props(
    Backoff.onStop(
      dbContextActorProps,
      childName = dbContextActorName,
      minBackoff = 1.seconds,
      maxBackoff = 10.seconds,
      randomFactor = 0.2 // adds 20% "noise" to vary the intervals slightly
    ).withAutoReset(10.seconds)
  )
  private val supervisorName = "dbContextActorSupervisor-" + Random.alphanumeric.take(5).mkString
  val backoffSupervisor = system.actorOf(backoffSupervisorProps, supervisorName)

  private val dbContextActorSelector = system.actorSelection(s"/user/$supervisorName/$dbContextActorName")

  def getDBContext: Future[DBContext] = dbContextActorSelector.ask(GetDBContext)(timeout).mapTo[DBContext]
}
