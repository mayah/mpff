package mpff.controllers

import java.util.UUID

import mpff.resources.BasicUserErrorCodes
import play.api.libs.json.JsBoolean
import play.api.libs.json.JsNull
import play.api.libs.json.JsNumber
import play.api.libs.json.JsString
import play.api.libs.json.JsUndefined
import play.api.libs.json.JsValue

trait MPFFRequestParameterTrait[ActionContext <: MPFFActionContext] {
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

  def jsonEnsureString(optJson: Option[JsValue], key: String): String = {
    jsonOptString(optJson, key) match {
      case None => throw new UserErrorControllerException(BasicUserErrorCodes.INVALID_JSON_PARAMETER, Map(key -> "invalid"))
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
      case None => throw new UserErrorControllerException(BasicUserErrorCodes.INVALID_JSON_PARAMETER, Map(key -> "invalid"))
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
}
