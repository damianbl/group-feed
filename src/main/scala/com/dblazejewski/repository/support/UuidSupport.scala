package com.dblazejewski.repository.support

import java.nio.ByteBuffer
import java.util.UUID

trait UuidSupport {
  implicit def fromByteArray(byteArray: Array[Byte]): UUID = {
    val bb = ByteBuffer.wrap(byteArray)
    new UUID(bb.getLong, bb.getLong)
  }

  implicit def toByteArray(id: UUID): Array[Byte] =
    ByteBuffer.allocate(16).putLong(id.getMostSignificantBits).putLong(id.getLeastSignificantBits).array
}
