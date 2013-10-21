package mpff.sessions

import play.api.mvc.CookieBaker
import play.api.Play

case class LongLiveSession(data: Map[String, String] = Map.empty[String, String]) {
  def get(key: String) = data.get(key)
  def isEmpty: Boolean = data.isEmpty
  def +(kv: (String, String)) = copy(data + kv)
  def -(key: String) = copy(data - key)
  def apply(key: String) = data(key)
}

object LongLiveSession extends CookieBaker[LongLiveSession] {
  private val TWO_WEEKS = 60 * 60 * 24 * 14
  val COOKIE_NAME = Play.maybeApplication.flatMap(_.configuration.getString("longlivesession.cookieName")).getOrElse("PLAY_LONGLIVE_SESSION")
  val emptyCookie = new LongLiveSession
  override val isSigned = true
  override def secure = Play.maybeApplication.flatMap(_.configuration.getBoolean("longlivesession.secure")).getOrElse(false)
  override val maxAge = Play.maybeApplication.flatMap(_.configuration.getInt("longlivesession.maxAge")).orElse(Some(TWO_WEEKS))
  override val httpOnly = Play.maybeApplication.flatMap(_.configuration.getBoolean("longlivesession.httpOnly")).getOrElse(true)
  override def path = Play.maybeApplication.flatMap(_.configuration.getString("application.context")).getOrElse("/")
  override def domain = Play.maybeApplication.flatMap(_.configuration.getString("longlivesession.domain"))

  def deserialize(data: Map[String, String]) = new LongLiveSession(data)

  def serialize(session: LongLiveSession) = session.data
}
