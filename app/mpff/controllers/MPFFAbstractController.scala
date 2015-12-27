package mpff.controllers

import mpff.resources.BasicServerErrorCodes
import mpff.resources.ServerErrorCode
import mpff.resources.UserErrorCode
import mpff.sessions.LongLiveSession
import play.api.Logger
import play.api.mvc.Action
import play.api.mvc.AnyContent
import play.api.mvc.Controller
import play.api.mvc.Request
import play.api.mvc.Result

abstract class MPFFAbstractController[ActionContext <: MPFFActionContext] extends Controller {
  // A user has to implement this function.
  def prepareActionContext(request: Request[AnyContent]): ActionContext

  object MPFFAction {
    def apply[A](block: (ActionContext => Result)) = Action { request: Request[AnyContent] =>
      val beginTime = System.currentTimeMillis()
      implicit val context = prepareActionContext(request)

      try {
        block(context)
      } catch {
        case e: ControllerException => renderException(e)
        case e: Exception =>
          Logger.warn("Unknown error: ", e)
          renderError(BasicServerErrorCodes.ERROR_UNKNOWN, Some(e))
      } finally {
        val endTime = System.currentTimeMillis()
        Logger.info(request.uri + " took " + (endTime - beginTime) + "[msec] to process.")
      }
    }
  }

  protected def finalizeResult(result: Result)(implicit context: ActionContext): Result = {
    val r1 = result.withSession(context.sessionValues.reverse: _*)
    val r2 = if (context.headers.isEmpty) r1 else r1.withHeaders(context.headers: _*)
    val r3 = r2.flashing(context.flashingValues: _*)

    if (context.longliveSessionValues.isEmpty) {
      return r3.discardingCookies(LongLiveSession.discard)
    } else {
      val longliveSession = LongLiveSession(context.longliveSessionValues.toMap)
      return r3.withCookies(LongLiveSession.encodeAsCookie(longliveSession))
    }
  }

  // ----------------------------------------------------------------------
  // Rendering

  protected def renderException(e: ControllerException)(implicit context: ActionContext): Result = {
    e.statusCode match {
      case 401 => renderLoginRequired()
      case 403 => renderForbidden()
      case 404 => renderNotFound()
      case _   =>
        e match {
          case se: ServerErrorControllerException => renderError(se.errorCode, se.optCause, se.optInfo);
          case ue: UserErrorControllerException => renderInvalid(ue.errorCode, ue.optCause, ue.optInfo);
        }
    }
  }

  protected def renderInvalid(ec: UserErrorCode, e: Option[Throwable] = None, optionalInfo: Option[Map[String, String]] = None)(implicit context: ActionContext): Result
  protected def renderError(ec: ServerErrorCode, e: Option[Throwable] = None, optionalInfo: Option[Map[String, String]] = None)(implicit context: ActionContext): Result
  protected def renderLoginRequired()(implicit context: ActionContext): Result
  protected def renderForbidden()(implicit context: ActionContext): Result
  protected def renderNotFound()(implicit context: ActionContext): Result
}
