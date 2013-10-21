package mpff.resources

import play.api.i18n.Messages

abstract class ServerErrorCode(val descriptionId: String, val statusCode: Int) {
  def description = Messages(descriptionId)
}

object ServerErrorUnknown extends ServerErrorCode("error.unknown", 500)
