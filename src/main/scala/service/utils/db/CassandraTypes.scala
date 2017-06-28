package service.utils.db

import com.datastax.driver.core.{BoundStatement, Row}

/**
 * Project: rest-ads
 * Package: service.utils.db
 * Created by asoloviov on 6/28/17 6:15 PM.
 */
trait CassandraTypes extends CassandraTypesMapping {

  class ColumnMapper[T](name: String)(implicit tm: TypeMapping[T], to: Either[BoundStatement, Row]) {
    def apply(value: T): BoundStatement = to match {
      case Right(_) => throw new IllegalArgumentException("Statement field assignment used to the ResultSet Row object")
      case Left(s) => tm.set(s, name, value)
    }

    def :=(value: T) = apply(value)

    def apply(): T = to match {
      case Right(r) => tm.get(r, name)
      case Left(_) => throw new IllegalArgumentException("ResultSet Row field value extractor used to the Statement object")
    }
  }

  def column[T](name: String)(implicit tm: TypeMapping[T], to: Either[BoundStatement, Row]) = new ColumnMapper[T](name)
}
