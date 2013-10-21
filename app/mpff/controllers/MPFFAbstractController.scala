package mpff.controllers

import java.lang.IllegalArgumentException
import java.util.UUID

import mpff.resources.INVALID_JSON_PARAMETER
import mpff.resources.INVALID_QUERY_PARAMETER
import mpff.resources.ServerErrorCode
import mpff.resources.ServerErrorUnknown
import mpff.resources.UserErrorCode
import mpff.sessions.LongLiveSession
import play.api.Logger
import play.api.libs.json.JsBoolean
import play.api.libs.json.JsNull
import play.api.libs.json.JsNumber
import play.api.libs.json.JsString
import play.api.libs.json.JsUndefined
import play.api.libs.json.JsValue
import play.api.mvc.Action
import play.api.mvc.AnyContent
import play.api.mvc.Controller
import play.api.mvc.Request
import play.api.mvc.SimpleResult

abstract class MPFFAbstractController[ActionContext <: MPFFActionContext] extends Controller {

  // A user has to implement this function.
  def prepareActionContext(request: Request[AnyContent]): ActionContext

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
  // Input Parameters (Query)

  def paramString(key: String)(implicit context: ActionContext): Option[String] = {
    context.request.queryString.get(key) match {
      case None => None
      case Some(values) => values.headOption
    }
  }

  def paramMultipleString(key: String)(implicit context: ActionContext): Option[Seq[String]] = {
    context.request.queryString.get(key)
  }

  def paramUUID(key: String)(implicit context: ActionContext): Option[UUID] = {
    paramString(key) match {
      case None => None
      case Some(x) => parseUUID(x)
    }
  }

  def ensureParamString(key: String)(implicit context: ActionContext): String = {
    paramString(key) match {
      case None => throw new UserErrorControllerException(INVALID_QUERY_PARAMETER, Map(key -> "missing"))
      case Some(x) => x
    }
  }

  def ensureParamUUID(key: String)(implicit context: ActionContext): UUID = {
    paramUUID(key) match {
      case None => throw new UserErrorControllerException(INVALID_QUERY_PARAMETER, Map(key -> "invalid"))
      case Some(x) => x
    }
  }

  // ----------------------------------------------------------------------
  // Input Parameters (json string)

  def jsonOptString(optJson: Option[JsValue], key: String): Option[String] = {
    optJson match {
      case None => None
      case Some(x) =>
        (x \ key).asOpt[JsString] match {
          case None => None
          case Some(x) => Some(x.value)
        }
    }
  }

  /**
   * None if key is missing.
   * Some(None) if key exists, but value is null etc.
   * Some(Some(x)) if key exists and value is string.
   */
  def jsonOptOptString(optJson: Option[JsValue], key: String): Option[Option[String]] = {
    optJson match {
      case None => None
      case Some(x) => (x \ key) match {
        case _: JsUndefined => None
        case JsNull => Some(None)
        case JsString(str) => Some(Some(str))
        case _ => None
      }
    }
  }

  def jsonEensureString(optJson: Option[JsValue], key: String): String = {
    jsonOptString(optJson, key) match {
      case None => throw new UserErrorControllerException(INVALID_JSON_PARAMETER, Map(key -> "invalid"))
      case Some(x) => x
    }
  }

  def jsonOptInt(optJson: Option[JsValue], key: String): Option[Int] = {
    optJson match {
      case None => None
      case Some(json) => (json \ key) match {
        case x: JsString => try { Some(x.value.toInt) } catch { case e: NumberFormatException => None }
        case x: JsNumber => try { Some(x.value.toInt) } catch { case e: NumberFormatException => None }
        case _ => None
      }
    }
  }

  def jsonEnsureInt(optJson: Option[JsValue], key: String): Int = {
    jsonOptInt(optJson, key) match {
      case None => throw new UserErrorControllerException(INVALID_JSON_PARAMETER, Map(key -> "invalid"))
      case Some(x) => x
    }
  }

  def jsonOptBoolean(optJson: Option[JsValue], key: String): Option[Boolean] = {
    optJson match {
      case None => None
      case Some(json) => (json \ key) match {
        case JsBoolean(x) => Some(x)
        case JsString(x) => parseBoolean(x)
        case _ => None
      }
    }
  }

  def jsonEnsureBoolean(optJson: Option[JsValue], key: String): Boolean = {
    jsonOptBoolean(optJson, key) match {
      case None => throw new UserErrorControllerException(INVALID_JSON_PARAMETER, Map(key -> "invalid"))
      case Some(x) => x
    }
  }

  def jsonOptUUID(optJson: Option[JsValue], key: String): Option[UUID] = {
    jsonOptString(optJson, key) match {
      case None => None
      case Some(x) => parseUUID(x)
    }
  }

  def jsonOptOptUUID(optJson: Option[JsValue], key: String): Option[Option[UUID]] = {
    jsonOptOptString(optJson, key) match {
      case None => None
      case Some(None) => Some(None)
      case Some(Some(x)) => Some(parseUUID(x))
    }
  }

  def jsonEnsureUUID(optJson: Option[JsValue], key: String): UUID = {
    jsonOptUUID(optJson, key) match {
      case None => throw new UserErrorControllerException(INVALID_JSON_PARAMETER, Map(key -> "invalid"))
      case Some(x) => x
    }
  }

  // ----------------------------------------------------------------------
  // Input Parameter parsing

  private def parseBoolean(value: String): Option[Boolean] = {
    value.toLowerCase() match {
      case "y" | "yes" | "t" | "true"  | "on" => Some(true)
      case "n" | "no"  | "f" | "false" | "off" => Some(false)
      case _ => None
    }
  }

  private def parseUUID(value: String): Option[UUID] = {
    try {
      Some(UUID.fromString(value))
    } catch {
      case e: IllegalArgumentException => None
    }
  }

  // ----------------------------------------------------------------------
  // Rendering

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
