package mpff.models

import java.util.UUID

import org.joda.time.DateTime

import anorm.Column
import anorm.MayErr.eitherToError
import anorm.MetaDataItem
import anorm.TypeDoesNotMatch

trait AnormModelSupport {
  implicit def rowToUUID: Column[UUID] = {
    Column.nonNull[UUID] { (value, meta) =>
      val MetaDataItem(qualified, nullable, clazz) = meta
      value match {
        case uuid: UUID => Right(uuid)
        case _ => Left(TypeDoesNotMatch("Cannot convert " + value + ":" + value.asInstanceOf[AnyRef].getClass + " to UUID for column " + qualified))
      }
    }
  }

  implicit def rowToDateTime: Column[DateTime] = {
    Column.nonNull { (value, meta) =>
      val MetaDataItem(qualified, nullable, clazz) = meta
      value match {
        case timestamp: java.sql.Timestamp => Right(new DateTime(timestamp.getTime()))
        case date: java.sql.Date => Right(new DateTime(date.getTime()))
        case _ => Left(TypeDoesNotMatch("Cannot convert " + value + ":" + value.asInstanceOf[AnyRef].getClass))
      }
    }
  }

  implicit def rowToSeqString: Column[Seq[String]] = Column.nonNull { (value, meta) =>
    val MetaDataItem(qualified, nullable, clazz) = meta
    value match {
      case arr: java.sql.Array => Right(arr.getArray.asInstanceOf[Array[String]].toSeq)
      case _ => Left(TypeDoesNotMatch("Cannot convert " + value + ":" + value.asInstanceOf[AnyRef].getClass + " to Seq[String] for column " + qualified))
    }
  }

  def escapeLIKEString(str: String): String = {
    str.replaceAll("\\\\", "\\\\\\\\").replaceAll("%", "\\\\%").replaceAll("_", "\\\\_");
  }
}
