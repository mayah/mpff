package mpff.models

import java.util.UUID
import java.lang.IllegalArgumentException

class Id(val id: UUID) {
  def this(idStr: String) = this(UUID.fromString(idStr))

  override def toString() = id.toString()
}

