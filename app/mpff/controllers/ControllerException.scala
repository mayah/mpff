package mpff.controllers

import mpff.resources.ServerErrorCode
import mpff.resources.UserErrorCode

abstract class ControllerException extends Exception {
  def optServerErrorCode: Option[ServerErrorCode]
  def optUserErrorCode: Option[UserErrorCode]
  def statusCode: Int
  def optCause: Option[Throwable]
}

case class ServerErrorControllerException(
    val errorCode: ServerErrorCode,
    val optCause: Option[Throwable] = None,
    val optInfo: Option[Map[String, String]] = None) extends ControllerException {
  def this(ec: ServerErrorCode, cause: Throwable) = this(ec, Option(cause))
  def this(ec: ServerErrorCode, info: Map[String, String]) = this(ec, None, Option(info))
  def this(ec: ServerErrorCode, cause: Throwable, info: Map[String, String]) = this(ec, Option(cause), Option(info))
  override def optServerErrorCode = Some(errorCode)
  override def optUserErrorCode = None
  override def statusCode = errorCode.statusCode
}

case class UserErrorControllerException(
    val errorCode: UserErrorCode,
    val optCause: Option[Throwable] = None,
    val optInfo: Option[Map[String, String]] = None) extends ControllerException {
  def this(ec: UserErrorCode, cause: Throwable) = this(ec, Option(cause))
  def this(ec: UserErrorCode, info: Map[String, String]) = this(ec, None, Option(info))
  def this(ec: UserErrorCode, cause: Throwable, info: Map[String, String]) = this(ec, Option(cause), Option(info))
  override def optServerErrorCode = None
  override def optUserErrorCode = Some(errorCode)
  override def statusCode = errorCode.statusCode
}
