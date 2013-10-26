package mpff.controllers

import play.api.mvc.AnyContent
import play.api.mvc.Request

class MPFFActionContext(val request: Request[AnyContent],
                        val sessionToken: String,
                        val sessionId: String) {
  // ----------------------------------------------------------------------
  // Session related methods
  var sessionValues: List[(String, String)] = List.empty
  def addSessionValue(key: String, value: String) {
    sessionValues = (key, value) :: sessionValues
  }

  // Removes all sessionValues having |key|, and adds (key, value).
  def setSessionValue(key: String, value: String) {
    sessionValues = (key, value) :: sessionValues.filter { case (k, v) => k != key }
  }

  def discardSession() {
    sessionValues = List.empty
  }

  // ----------------------------------------------------------------------
  // LongLive Session related methods. Actually this is a cookie.

  var longliveSessionValues: List[(String, String)] = List.empty
  def addLongLiveSessionValue(key: String, value: String) {
    longliveSessionValues = (key, value) :: longliveSessionValues
  }
  def discardLongLiveSession() {
    longliveSessionValues = List.empty
  }

  // ----------------------------------------------------------------------
  // Misc.

  var flashingValues: List[(String, String)] = List.empty
  def addFlashing(key: String, value: String) {
    flashingValues = (key, value) :: flashingValues
  }

  var headers: List[(String, String)] = List.empty
  def addHeader(key: String, value: String) {
    headers = (key -> value) :: headers
  }
}
