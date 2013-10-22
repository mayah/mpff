package mpff.controllers

import play.api.mvc.AnyContent
import play.api.mvc.Request

class MPFFActionContext(val request: Request[AnyContent]) {
  var sessionValues: List[(String, String)] = List.empty
  def addSessionValue(key: String, value: String) {
    sessionValues = (key, value) :: sessionValues
  }
  def discardSession() {
    sessionValues = List.empty
  }

  var longliveSessionValues: List[(String, String)] = List.empty
  def addLongLiveSessionValue(key: String, value: String) {
    longliveSessionValues = (key, value) :: longliveSessionValues
  }
  def discardLongLiveSession() {
    longliveSessionValues = List.empty
  }

  var flashingValues: List[(String, String)] = List.empty
  def addFlashing(key: String, value: String) {
    flashingValues = (key, value) :: flashingValues
  }

  var headers: List[(String, String)] = List.empty
  def addHeader(key: String, value: String) {
    headers = (key -> value) :: headers
  }
}
