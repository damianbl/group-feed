package com.dblazejewski.domain

import java.util.UUID

final case class Group(id: UUID, name: String) extends IdEntity

final case class UserGroup(id: UUID, userId: UUID, groupId: UUID) extends IdEntity

object Group {
  def create(name: String) = Group(UUID.randomUUID, name)
}
