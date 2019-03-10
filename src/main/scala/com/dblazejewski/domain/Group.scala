package com.dblazejewski.domain

final case class Group(id: Option[Int], name: String) extends IdEntity

final case class Groups(groups: Seq[Group])

final case class UserParticipatesInGroup(
  id: Option[Int],
  userId: Int,
  groupId: Int) extends IdEntity
