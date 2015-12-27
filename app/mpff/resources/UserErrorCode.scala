package mpff.resources

import play.api.i18n.Messages

case class UserErrorCode(val descriptionId: String, val statusCode: Int = 400) {
  def description(implicit messages: Messages) = Messages(descriptionId)
}

trait MPFFUserErrorCodes {
  // --- 400
  val INVALID_UNKNOWN = UserErrorCode("invalid.unknown")

  val INVALID_QUERY_PARAMETER = UserErrorCode("invalid.query.parameter")
  val INVALID_JSON_PARAMETER = UserErrorCode("invalid.json.parameter")

  val INVALID_SESSION_TOKEN = UserErrorCode("invalid.session.token")

  val INVALID_AUTH_PASSWORD = UserErrorCode("invalid.auth.password")

  // --- 401
  val INVALID_LOGIN_REQUIRED = UserErrorCode("invalid.login.required", 401)

  // --- 403
  val INVALID_FORBIDDEN = UserErrorCode("invalid.forbidden", 403)
  val INVALID_ADMIN_REQUIRED = UserErrorCode("invalid.admin.required", 403)

  // --- 404
  val INVALID_NOT_FOUND = UserErrorCode("invalid.notfound", 404)
}

object BasicUserErrorCodes extends MPFFUserErrorCodes
