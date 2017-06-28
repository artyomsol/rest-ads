package service.utils.db

import java.util.Date

import com.datastax.driver.core.{BoundStatement, Row}
import org.joda.time.DateTime

import scala.collection.JavaConverters._
import scala.reflect.ClassTag

/**
 * Project: rest-ads
 * Package: service.utils.db
 * Created by asoloviov on 6/28/17 6:11 PM.
 */
trait CassandraTypesMapping {

  implicit class PimpBoundStatement(statement: BoundStatement) {
    def setOption[T: ClassTag](name: String, vOpt: Option[T]): BoundStatement = {
      val clazz = implicitly[ClassTag[T]].runtimeClass
      vOpt match {
        case None => statement.setToNull(name)
        case Some(v) if clazz.isInstance(v) => statement.set(name, v.asInstanceOf[T], clazz.asInstanceOf[Class[T]])
      }
    }

    def setDateTime(name: String, v: DateTime): BoundStatement = statement.setTimestamp(name, v.toDate)

    def setDateTime(i: Int, v: DateTime): BoundStatement = statement.setTimestamp(i, v.toDate)

    def setDateTime(name: String, vOpt: Option[DateTime]): BoundStatement = vOpt match {
      case None => statement.setTimestamp(name, new Date(0L))
      case Some(v) => statement.setTimestamp(name, v.toDate)
    }

    def setDateTime(i: Int, vOpt: Option[DateTime]): BoundStatement = vOpt match {
      case None => statement.setTimestamp(i, new Date(0L))
      case Some(v) => statement.setTimestamp(i, v.toDate)
    }
  }

  implicit class PimpResultRow(row: Row) {
    def getOption[T: ClassTag](name: String): Option[T] =
      if (row.isNull(name)) None else Option(row.get(name, implicitly[ClassTag[T]].runtimeClass.asInstanceOf[Class[T]]))

    def getDateTime(name: String): DateTime = getDateTime(row.getColumnDefinitions.getIndexOf(name))

    def getDateTime(i: Int): DateTime = try {
      if (row.isNull(i)) throw new IllegalArgumentException
      val date = row.getTimestamp(i)
      new DateTime(date)
    } catch {
      case e: IllegalArgumentException =>
        throw new IllegalArgumentException(s"field '${row.getColumnDefinitions.getName(i)}' contains NULL or incorrect data. Date type expected.", e)
      case e: IndexOutOfBoundsException =>
        throw new IndexOutOfBoundsException(s"field index '$i' not found")
    }

    def getDateTimeOpt(i: Int): Option[DateTime] = try {
      if (row.isNull(i) || row.getTimestamp(i).getTime == 0L) None
      else Some(getDateTime(i))
    } catch {
      case e: IndexOutOfBoundsException =>
        throw new IndexOutOfBoundsException(s"field index '$i' not found")
    }

    def getDateTimeOpt(name: String): Option[DateTime] = getDateTimeOpt(row.getColumnDefinitions.getIndexOf(name))
  }

  abstract class OptionTypeMapping[T <: Any : ClassTag] extends TypeMapping[Option[T]] {
    override def set(s: BoundStatement, name: String, value: Option[T]): BoundStatement = s.setOption[T](name, value)

    override def get(r: Row, name: String): Option[T] = r.getOption[T](name)
  }

  implicit object StringColumnMapping extends TypeMapping[String] {
    override def set(s: BoundStatement, name: String, value: String): BoundStatement = s.setString(name, value)

    override def get(r: Row, name: String): String = r.getString(name) match {
      case null => throw new NoSuchElementException(s"Field '$name' value is empty.")
      case value => value
    }
  }

  implicit object OptionStringColumnMapping extends OptionTypeMapping[String]

  implicit object BooleanColumnMapping extends TypeMapping[Boolean] {
    override def set(s: BoundStatement, name: String, value: Boolean): BoundStatement = s.setBool(name, value)

    override def get(r: Row, name: String): Boolean =
      if (r.isNull(name)) throw new NoSuchElementException(s"Field '$name' value is empty.") else r.getBool(name)
  }

  implicit object OptionBooleanColumnMapping extends OptionTypeMapping[Boolean]

  implicit object SetOfStringColumnMapping extends TypeMapping[Set[String]] {
    override def set(s: BoundStatement, name: String, value: Set[String]): BoundStatement = s.setSet[String](name, value.asJava)

    override def get(r: Row, name: String): Set[String] = r.getSet[String](name, classOf[String]).asScala.toSet
  }

  implicit object DateTimeColumnMapping extends TypeMapping[DateTime] {
    override def set(s: BoundStatement, name: String, value: DateTime): BoundStatement = s.setDateTime(name, value)

    override def get(r: Row, name: String): DateTime =
      if (r.isNull(name)) throw new NoSuchElementException(s"Field '$name' value is empty.") else r.getDateTime(name)
  }

  implicit object OptionDateTimeColumnMapping extends TypeMapping[Option[DateTime]] {
    override def set(s: BoundStatement, name: String, value: Option[DateTime]): BoundStatement = s.setDateTime(name, value)

    override def get(r: Row, name: String): Option[DateTime] = r.getDateTimeOpt(name)
  }

  implicit object LongColumnMapping extends TypeMapping[Long] {
    override def set(s: BoundStatement, name: String, value: Long): BoundStatement = s.setLong(name, value)

    override def get(r: Row, name: String): Long = r.getLong(name)
  }

  implicit object OptionLongColumnMapping extends OptionTypeMapping[Long]
}

