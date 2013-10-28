package mpff.controllers

package controllers.action

import mpff.controllers.MPFFAbstractController
import mpff.controllers.MPFFActionContext
import mpff.resources.ServerErrorCode
import mpff.resources.UserErrorCode
import play.api.mvc.SimpleResult
import play.api.templates.Html

abstract class MPFFAbstractActionController[ActionContext <: MPFFActionContext] extends MPFFAbstractController[ActionContext] {
  def renderText(text: String)(implicit context: ActionContext) = {
    finalizeResult(Ok(text))
  }

  def renderHTML(content: Html)(implicit context: ActionContext) = {
    finalizeResult(Ok(content))
  }

  def renderByteArray(content: Array[Byte])(implicit context: ActionContext) = {
    finalizeResult(Ok(content))
  }

  def renderRedirect(url: String)(implicit context: ActionContext) = {
    finalizeResult(Redirect(url))
  }

  override protected def renderInvalid(ec: UserErrorCode, e: Option[Throwable] = None, optInfo: Option[Map[String, String]] = None)(implicit context: ActionContext): SimpleResult = {
    e match {
      case None => finalizeResult(BadRequest)
      case Some(x) => finalizeResult(Redirect("/invalid?errorCode=" + ec.descriptionId))
    }
  }

  override protected def renderError(ec: ServerErrorCode, e: Option[Throwable] = None, optInfo: Option[Map[String, String]] = None)(implicit context: ActionContext): SimpleResult = {
    e match {
      case None => finalizeResult(InternalServerError)
      case Some(x) => finalizeResult(Redirect("/error?errorCode=" + ec.descriptionId))
    }
  }

  override protected def renderLoginRequired()(implicit context: ActionContext): SimpleResult = finalizeResult(Redirect("/loginRequired"))
  override protected def renderForbidden()(implicit context: ActionContext): SimpleResult = finalizeResult(Forbidden)
  override protected def renderNotFound()(implicit context: ActionContext): SimpleResult = finalizeResult(NotFound)
}
