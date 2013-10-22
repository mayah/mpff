package mpff.resources

import play.api.i18n.Messages

case class UserErrorCode(val descriptionId: String, val statusCode: Int = 400) {
  def description = Messages(descriptionId)
}

trait MPFFUserErrorCodes {
  val INVALID_UNKNOWN = UserErrorCode("invalid.unknown")

  val INVALID_QUERY_PARAMETER = UserErrorCode("invalid.query.parameter")
  val INVALID_JSON_PARAMETER = UserErrorCode("invalid.json.parameter")
}

object BasicUserErrorCodes extends MPFFUserErrorCodes