package service.utils.db

import akka.actor.{Actor, ActorLogging, Props, Stash}

import scala.concurrent.Future
import scala.util.{Failure, Success}

/**
 * Project: rest-ads
 * Package: service.utils.db
 * Created by asoloviov on 6/30/17 11:17 AM.
 */
class DBContextActor(contextCreator: => Future[DBContext]) extends Actor with Stash with ActorLogging {

  import DBContextActor._
  import context.dispatcher

  var dbContextOpt: Option[DBContext] = None
  val initialized: Receive = {
    case GetDBContext =>
      dbContextOpt.get
      log.debug("responding with " + dbContextOpt.get)
    case InitDBContext | _: InitWithDBContext => //ignore
  }
  val notInitialized: Receive = {
    case InitDBContext =>
      log.debug("got InitDBContext")
      contextCreator onComplete {
        case Success(dbContext) =>
          log.debug("initialization result:{}", dbContextOpt)
          self ! InitWithDBContext(dbContext)
        case Failure(e) =>
          log.warning("DBContext init failed:" + e.getMessage)
          // throw it to let supervisor restart me with backoff delay
          context.stop(self)
      }
    case InitWithDBContext(dbContext) =>
      log.debug("got InitWithDBContext:" + dbContext)
      dbContextOpt = Some(dbContext)
      context.become(initialized)
      unstashAll()
    case GetDBContext => stash()
  }
  val receive: Receive = notInitialized

  override def preStart(): Unit =
    self ! InitDBContext
}

object DBContextActor {

  case object GetDBContext

  case object InitDBContext

  case class InitWithDBContext(dbContext: DBContext)

  def props(contextCreator: => Future[DBContext]) = Props(new DBContextActor(contextCreator))
}
