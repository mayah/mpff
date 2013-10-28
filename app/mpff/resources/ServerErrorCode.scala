package mpff.resources

import play.api.i18n.Messages

case class ServerErrorCode(val descriptionId: String, val statusCode: Int = 500) {
  def description = Messages(descriptionId)
}

trait MPFFServerErrorCodes {
  val ERROR_UNKNOWN = ServerErrorCode("error.unknown")
}

object BasicServerErrorCodes extends MPFFServerErrorCodes
