package mpff.controllers

package controllers.action

import play.api.Logger
import play.api.mvc.SimpleResult
import play.api.templates.Html
import mpff.resources.ServerErrorCode
import mpff.resources.UserErrorCode

abstract class MPFFAbstrctActionController[ActionContext <: MPFFActionContext] extends MPFFAbstractController[ActionContext] {
  protected def render(content: Html): SimpleResult = Ok(content)

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
