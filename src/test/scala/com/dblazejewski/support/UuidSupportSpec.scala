package com.dblazejewski.support

import java.util.UUID
import org.scalatest._

class UuidSupportSpec extends FlatSpec with Matchers {

  "A UuidSupport.getUUID" should "return correct UUID for string without dashes" in {
    val uuidString = "33f9ca3e0a48488dbeb61704e84f93bd"
    UuidSupport.getUUID(uuidString) should be(UUID.fromString("33f9ca3e-0a48-488d-beb6-1704e84f93bd"))
  }

  "A UuidSupport.getUUID" should "return correct UUID for string with dashes" in {
    val uuidString = "33f9ca3e-0a48-488d-beb6-1704e84f93bd"
    UuidSupport.getUUID(uuidString) should be(UUID.fromString("33f9ca3e-0a48-488d-beb6-1704e84f93bd"))
  }

  it should "throw NoSuchElementException if not a valid UUID" in {
    val uuidString = "3r3f"
    a[NoSuchElementException] should be thrownBy {
      UuidSupport.getUUID(uuidString)
    }
  }
}
