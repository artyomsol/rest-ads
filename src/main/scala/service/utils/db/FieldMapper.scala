package service.utils.db

import com.datastax.driver.core.{BoundStatement, Row}

/**
 * Project: rest-ads
 * Package: service.utils.db
 * Created by asoloviov on 6/28/17 6:30 PM.
 */
sealed trait FieldMapper {
  val to: Either[BoundStatement, Row]
}

abstract class RowMapper[R](r: Row) extends FieldMapper {
  implicit val to: Either[BoundStatement, Row] = Right(r)

  def toRecord: R
}

abstract class StatementMapper[R](s: BoundStatement) extends FieldMapper {
  implicit val to: Either[BoundStatement, Row] = Left(s)

  def fromRecord(record: R): BoundStatement
}
