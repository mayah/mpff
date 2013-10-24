package mpff.controllers

import java.util.UUID

trait MPFFParserTrait {
  def parseBoolean(value: String): Option[Boolean] = {
    value.toLowerCase() match {
      case "y" | "yes" | "t" | "true"  | "on" => Some(true)
      case "n" | "no"  | "f" | "false" | "off" => Some(false)
      case _ => None
    }
  }

  def parseUUID(value: String): Option[UUID] = {
    try {
      Some(UUID.fromString(value))
    } catch {
      case e: IllegalArgumentException => None
    }
  }
}
