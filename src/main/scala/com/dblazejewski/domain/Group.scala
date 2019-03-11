package com.dblazejewski.domain

final case class Group(id: Option[Long], name: String) extends IdEntity

final case class Groups(groups: Seq[Group])

final case class UserParticipatesInGroup(id: Option[Long], userId: Long, groupId: Long) extends IdEntity

object Group {
  def create(name: String) = Group(None, name)
}
