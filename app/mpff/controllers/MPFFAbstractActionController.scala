package mpff.controllers

import mpff.resources.ServerErrorCode
import mpff.resources.UserErrorCode
import play.api.i18n.I18nSupport
import play.api.mvc.Result
import play.twirl.api.Html

abstract class MPFFAbstractActionController[ActionContext <: MPFFActionContext]
    extends MPFFAbstractController[ActionContext]
    with I18nSupport {
  protected def renderText(text: String)(implicit context: ActionContext) = {
    finalizeResult(Ok(text))
  }

  protected def renderHTML(content: Html)(implicit context: ActionContext) = {
    finalizeResult(Ok(content))
  }

  protected def renderByteArray(content: Array[Byte])(implicit context: ActionContext) = {
    finalizeResult(Ok(content))
  }

  protected def renderRedirect(url: String)(implicit context: ActionContext) = {
    finalizeResult(Redirect(url))
  }

  override protected def renderInvalid(ec: UserErrorCode, e: Option[Throwable] = None, optInfo: Option[Map[String, String]] = None)(implicit context: ActionContext): Result = {
    e match {
      case None => finalizeResult(BadRequest)
      case Some(x) => finalizeResult(Redirect("/invalid?errorCode=" + ec.descriptionId))
    }
  }

  override protected def renderError(ec: ServerErrorCode, e: Option[Throwable] = None, optInfo: Option[Map[String, String]] = None)(implicit context: ActionContext): Result = {
    e match {
      case None => finalizeResult(InternalServerError)
      case Some(x) => finalizeResult(Redirect("/error?errorCode=" + ec.descriptionId))
    }
  }

  override protected def renderLoginRequired()(implicit context: ActionContext): Result = finalizeResult(Redirect("/loginRequired"))
  override protected def renderForbidden()(implicit context: ActionContext): Result = finalizeResult(Forbidden)
  override protected def renderNotFound()(implicit context: ActionContext): Result = finalizeResult(NotFound)
}
