package mpff.resources

import play.api.i18n.Messages

abstract class UserErrorCode(val descriptionId: String, val statusCode: Int) {
  def description = Messages(descriptionId)
}
