package mpff.controllers

import java.util.UUID

import mpff.resources.BasicConstants
import mpff.resources.BasicUserErrorCodes
import play.api.libs.json.JsArray
import play.api.libs.json.JsBoolean
import play.api.libs.json.JsNull
import play.api.libs.json.JsNumber
import play.api.libs.json.JsString
import play.api.libs.json.JsUndefined
import play.api.libs.json.JsValue

trait MPFFRequestParameterTrait[ActionContext <: MPFFActionContext] extends MPFFParserTrait {
  // ----------------------------------------------------------------------
  // Input Parameters (Query)

  def paramOptString(key: String)(implicit context: ActionContext): Option[String] = {
    context.request.queryString.get(key) match {
      case None => None
      case Some(values) => values.headOption
    }
  }

  def paramEnsureString(key: String)(implicit context: ActionContext): String = {
    paramOptString(key) match {
      case None => throw new UserErrorControllerException(BasicUserErrorCodes.INVALID_QUERY_PARAMETER, Map(key -> "missing"))
      case Some(x) => x
    }
  }

  def paramOptMultipleString(key: String)(implicit context: ActionContext): Option[Seq[String]] = {
    context.request.queryString.get(key)
  }

  def paramOptInt(key: String)(implicit context: ActionContext): Option[Int] = {
    paramOptString(key) match {
      case None => None
      case Some(x) => try { Some(x.toInt) } catch { case e: NumberFormatException => None }
    }
  }

  def paramEnsureInt(key: String)(implicit context: ActionContext): Int = {
    paramOptInt(key) match {
      case None => throw new UserErrorControllerException(BasicUserErrorCodes.INVALID_QUERY_PARAMETER, Map(key -> "invalid"))
      case Some(x) => x
    }
  }

  def paramOptUUID(key: String)(implicit context: ActionContext): Option[UUID] = {
    paramOptString(key) match {
      case None => None
      case Some(x) => parseUUID(x)
    }
  }

  def paramEnsureUUID(key: String)(implicit context: ActionContext): UUID = {
    paramOptUUID(key) match {
      case None => throw new UserErrorControllerException(BasicUserErrorCodes.INVALID_QUERY_PARAMETER, Map(key -> "invalid"))
      case Some(x) => x
    }
  }

  // ----------------------------------------------------------------------
  // Input Parameters (json string)

  def jsonOptString(key: String)(implicit context: ActionContext): Option[String] = {
    jsonOptString(context.request.body.asJson, key)
  }

  def jsonEnsureString(key: String)(implicit context: ActionContext): String = {
    jsonEnsureString(context.request.body.asJson, key)
  }

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
      case Some(json) => (json \ key).toOption match {
        case None => None
        case Some(JsNull) => Some(None)
        case Some(JsString(str)) => Some(Some(str))
        case _ => None
      }
    }
  }

  def jsonEnsureString(optJson: Option[JsValue], key: String): String = {
    jsonOptString(optJson, key) match {
      case None => throw new UserErrorControllerException(BasicUserErrorCodes.INVALID_JSON_PARAMETER, Map(key -> "invalid"))
      case Some(x) => x
    }
  }

  def jsonOptInt(optJson: Option[JsValue], key: String): Option[Int] = {
    optJson match {
      case None => None
      case Some(json) => (json \ key).toOption match {
        case Some(x: JsString) => try { Some(x.value.toInt) } catch { case e: NumberFormatException => None }
        case Some(x: JsNumber) => try { Some(x.value.toInt) } catch { case e: NumberFormatException => None }
        case _ => None
      }
    }
  }

  def jsonEnsureInt(optJson: Option[JsValue], key: String): Int = {
    jsonOptInt(optJson, key) match {
      case None => throw new UserErrorControllerException(BasicUserErrorCodes.INVALID_JSON_PARAMETER, Map(key -> "invalid"))
      case Some(x) => x
    }
  }

  def jsonOptBoolean(optJson: Option[JsValue], key: String): Option[Boolean] = {
    optJson match {
      case None => None
      case Some(json) => (json \ key).toOption match {
        case Some(JsBoolean(x)) => Some(x)
        case Some(JsString(x)) => parseBoolean(x)
        case _ => None
      }
    }
  }

  def jsonEnsureBoolean(optJson: Option[JsValue], key: String): Boolean = {
    jsonOptBoolean(optJson, key) match {
      case None => throw new UserErrorControllerException(BasicUserErrorCodes.INVALID_JSON_PARAMETER, Map(key -> "invalid"))
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
      case None => throw new UserErrorControllerException(BasicUserErrorCodes.INVALID_JSON_PARAMETER, Map(key -> "invalid"))
      case Some(x) => x
    }
  }

  def jsonOptJSONArray(optJson: Option[JsValue], key: String): Option[JsArray] = {
    optJson match {
      case None => None
      case Some(x) => (x \ key).asOpt[JsArray]
    }
  }

  def jsonEnsureJSONArray(optJson: Option[JsValue], key: String): JsArray = {
    jsonOptJSONArray(optJson, key).getOrElse {
      throw new UserErrorControllerException(BasicUserErrorCodes.INVALID_JSON_PARAMETER, Map(key -> "missing"))
    }
  }

  // ----------------------------------------------------------------------
  // Check session tokens

  def ensureValidSessionToken(optJson: Option[JsValue])(implicit context: ActionContext) {
    jsonOptString(optJson, BasicConstants.Session.TOKEN_KEY) match {
      case None => throw UserErrorControllerException(BasicUserErrorCodes.INVALID_SESSION_TOKEN)
      case Some(token) if (token != context.sessionToken) => throw UserErrorControllerException(BasicUserErrorCodes.INVALID_SESSION_TOKEN)
      case Some(_) => () // OK
    }
  }

}
