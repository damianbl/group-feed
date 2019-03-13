package com.dblazejewski.domain

import java.util.UUID

case class User(id: UUID, name: String) extends IdEntity

object User {
  def create(name: String) = User(UUID.randomUUID, name)
}
