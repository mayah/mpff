package mpff.resources

trait MPFFConstants {
  trait MPFFSession {
    val ID_KEY = "sessionId"
    val USER_ID_KEY = "userId"
    val TOKEN_KEY = "sessionToken"
  }
}

object BasicConstants extends MPFFConstants {
  object Session extends MPFFSession
}
