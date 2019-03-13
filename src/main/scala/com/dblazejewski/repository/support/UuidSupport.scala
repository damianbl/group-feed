package com.dblazejewski.repository.support

import java.nio.ByteBuffer
import java.util.UUID

trait UuidSupport {
  implicit def fromByteArray(byteArray: Array[Byte]): UUID = {
    val bb: ByteBuffer = ByteBuffer.wrap(byteArray)
    new UUID(bb.getLong, bb.getLong)
  }

  implicit def toByteArray(id: UUID): Array[Byte] = {
    val bb: ByteBuffer = ByteBuffer.allocate(16)
    bb.putLong(id.getMostSignificantBits).putLong(id.getLeastSignificantBits)
    bb.array
  }
}
