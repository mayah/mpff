package mpff.controllers

import play.api.mvc._
import scala.concurrent.Future
import play.api.Logger
import mpff.resources.ServerErrorCode
import mpff.resources.UserErrorCode
import mpff.resources.ServerErrorUnknown
import mpff.session.LongLiveSession

abstract class MPFFAbstractController[ActionContext <: MPFFActionContext] extends Controller {

  // A user has to implement this function.
  def prepareActionContext[A](request: Request[A]): ActionContext

  object MPFFAction {
    def apply[A](block: (Request[AnyContent] => ActionContext => SimpleResult)) = Action { request: Request[AnyContent] =>
      val beginTime = System.currentTimeMillis()
      implicit val context = prepareActionContext(request)

      try {
        block(request)(context)
      } catch {
        case e: ControllerException => renderException(e)
        case e: Exception => renderError(ServerErrorUnknown, Some(e))
      } finally {
        val endTime = System.currentTimeMillis()
        Logger.info(request.uri + " took " + (endTime - beginTime) + "[msec] to process.")
      }
    }
  }

  protected def finalizeResult(result: SimpleResult)(implicit context: ActionContext): SimpleResult = {
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

  protected def renderException(e: ControllerException)(implicit context: ActionContext): SimpleResult = {
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

  protected def renderInvalid(ec: UserErrorCode, e: Option[Throwable] = None, optionalInfo: Option[Map[String, String]] = None)(implicit context: ActionContext): SimpleResult
  protected def renderError(ec: ServerErrorCode, e: Option[Throwable] = None, optionalInfo: Option[Map[String, String]] = None)(implicit context: ActionContext): SimpleResult
  protected def renderLoginRequired()(implicit context: ActionContext): SimpleResult
  protected def renderForbidden()(implicit context: ActionContext): SimpleResult
  protected def renderNotFound()(implicit context: ActionContext): SimpleResult
}
