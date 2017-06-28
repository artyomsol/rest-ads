package service.utils.db

import com.datastax.driver.core._
import com.google.common.util.concurrent.{FutureCallback, Futures, ListenableFuture}

import scala.concurrent.{Future, Promise}

/**
 * Project: rest-ads
 * Package: service.utils.db
 * Created by asoloviov on 6/28/17 6:24 PM.
 */
class CassandraTable(val tableName: String)(implicit cassandraContext: CassandraContext) extends CassandraTypes {
  implicit val session: Session = cassandraContext.session
  protected val keyspace = cassandraContext.keyspace
  private val defaultReadConsistency = ConsistencyLevel.valueOf(cassandraContext.defaultReadConsistency)
  private val defaultWriteConsistency = ConsistencyLevel.valueOf(cassandraContext.defaultWriteConsistency)

  def prepare(query: String, consistencyLevel: ConsistencyLevel): PreparedStatement = {
    val toPrepare: RegularStatement = new SimpleStatement(query)
    session.prepare(toPrepare).setConsistencyLevel(consistencyLevel)
  }

  private def properConsistencyLevel(query: String) = if ( """^(update|insert|create|delete|drop|alter|truncate)""".r.findFirstIn(query.toLowerCase.trim).isDefined) defaultWriteConsistency else defaultReadConsistency

  def prepare(query: String): PreparedStatement = prepare(query, properConsistencyLevel(query))

  implicit class RichListenableFuture[T](lf: ListenableFuture[T]) {
    def toPromise: Future[T] = {
      val p = Promise[T]()
      Futures.addCallback(lf, new FutureCallback[T] {
        def onFailure(t: Throwable): Unit = p failure t

        def onSuccess(result: T): Unit = p success result
      })
      p.future
    }
  }

  implicit class PimpResultSet(rs: ResultSet) {
    def firstOption: Option[Row] = if (rs.isExhausted) None else Some(rs.one())
  }

}
