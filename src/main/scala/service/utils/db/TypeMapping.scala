package service.utils.db

import com.datastax.driver.core.{BoundStatement, Row}

/**
 * Project: rest-ads
 * Package: service.utils.db
 * Created by asoloviov on 6/28/17 6:11 PM.
 */
abstract class TypeMapping[T] {
  def set(s: BoundStatement, name: String, value: T): BoundStatement

  def get(r: Row, name: String): T
}