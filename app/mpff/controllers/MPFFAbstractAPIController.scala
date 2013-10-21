package mpff.controllers

import java.nio.charset.Charset

import mpff.resources.ServerErrorCode
import mpff.resources.UserErrorCode
import play.api.Logger
import play.api.libs.json.JsValue
import play.api.libs.json.Json
import play.api.mvc.SimpleResult

abstract class MPFFAbstractAPIController[ActionContext <: MPFFActionContext] extends MPFFAbstractController[ActionContext] {
  private val UTF8 = Charset.forName("UTF8")
  private val RESULT_KEY = "result"
  private val DESCRIPTION_KEY = "description"
  private val OPTIONAL_INFO = "optInfo"

  // ----------------------------------------------------------------------

  def renderOK()(implicit context: ActionContext): SimpleResult = {
    renderJson(Json.obj(RESULT_KEY -> "ok"))
  }

  def renderJson(obj: JsValue, status: Int = OK)(implicit context: ActionContext): SimpleResult = {
    finalizeResult(Status(status)(obj))
  }

  override protected def renderInvalid(ec: UserErrorCode, e: Option[Throwable], optInfo: Option[Map[String, String]])(implicit context: ActionContext): SimpleResult = {
    e match {
      case None => ()
      case Some(x) => Logger.info("renderInvalid", x)
    }

    val json = Json.obj(
      RESULT_KEY -> "invalid",
      DESCRIPTION_KEY -> ec.description,
      OPTIONAL_INFO -> Json.toJson(optInfo.getOrElse(Map()))
    )

    renderJson(json, BAD_REQUEST)
  }

  override protected def renderError(ec: ServerErrorCode, e: Option[Throwable], optInfo: Option[Map[String, String]])(implicit context: ActionContext): SimpleResult = {
    e match {
      case None => ()
      case Some(x) => Logger.info("renderInvalid", x)
    }

    val json = Json.obj(
      RESULT_KEY -> "error",
      DESCRIPTION_KEY -> ec.description,
      OPTIONAL_INFO -> Json.toJson(optInfo.getOrElse(Map()))
    )

    renderJson(json, INTERNAL_SERVER_ERROR)
  }

  override protected def renderLoginRequired()(implicit context: ActionContext): SimpleResult = {
    val json = Json.obj(
      RESULT_KEY -> "auth",
      DESCRIPTION_KEY -> "Login is required"
    )

    renderJson(json, UNAUTHORIZED).withHeaders(
      "WWW-Authenticate" -> "OAuth"
    )
  }

  override protected def renderForbidden()(implicit context: ActionContext): SimpleResult = {
    val json = Json.obj(
      RESULT_KEY -> "forbidden",
      DESCRIPTION_KEY -> "Forbidden action"
    )
    renderJson(json, FORBIDDEN)
  }

  override protected def renderNotFound()(implicit context: ActionContext): SimpleResult = {
    val json = Json.obj(
      RESULT_KEY -> "notfound",
      DESCRIPTION_KEY -> "Not found"
    )
    renderJson(json, NOT_FOUND)
  }

}
