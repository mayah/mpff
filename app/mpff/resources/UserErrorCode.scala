package mpff.resources

import play.api.i18n.Messages

abstract class UserErrorCode(val descriptionId: String, val statusCode: Int = 400) {
  def description = Messages(descriptionId)
}

object INVALID_QUERY_PARAMETER extends UserErrorCode("invalid.query.parameter")
object INVALID_JSON_PARAMETER extends UserErrorCode("invalid.json.parameter")
