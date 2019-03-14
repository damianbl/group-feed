package com.dblazejewski.support

import java.util.UUID

import scala.util.Try

object UuidSupport {
  def getUUID(str: String): UUID = getUUIDWithoutDashes(str) orElse uuidFromString(str) get

  private def uuidFromString(str: String) = Try(UUID.fromString(str)).toOption

  private val uuidRegexWithoutDashes =
    "([0-9a-fA-F]{8})([0-9a-fA-F]{4})([0-9a-fA-F]{4})([0-9a-fA-F]{4})([0-9a-fA-F]+)".r

  private def getUUIDWithoutDashes(str: String) =
    uuidFromString(uuidRegexWithoutDashes.replaceAllIn(str, "$1-$2-$3-$4-$5"))
}
