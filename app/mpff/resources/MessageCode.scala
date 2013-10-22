package mpff.resources

import play.api.i18n.Messages

case class MessageCode(val descriptionId: String) {
  def description = Messages(descriptionId)
}

trait MPFFMessageCodes {
  val MESSAGE_UNKNOWN = MessageCode("message.unknown")
}

object BasicMessageCodes extends MPFFMessageCodes
