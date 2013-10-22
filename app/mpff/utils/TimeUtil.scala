package mpff.utils

import org.joda.time.DateTime
import java.sql.Timestamp

// TimeUtil is used to obtain the current date time.
// For testing purpose, we would like to hook obtaining the current time.
object TimeUtil {
  var optCurrentDateTime: Option[DateTime] = None

  def resetCurrentDateTime() {
    optCurrentDateTime = None
  }

  def setCurrentDateTime(dateTime: DateTime) {
    optCurrentDateTime = Option(dateTime);
  }

  def currentDateTime(): DateTime = {
    optCurrentDateTime match {
      case None => new DateTime()
      case Some(x) => x
    }
  }

  def currentTimestamp(): Timestamp = {
    new Timestamp(currentDateTime().toDate().getTime())
  }
}