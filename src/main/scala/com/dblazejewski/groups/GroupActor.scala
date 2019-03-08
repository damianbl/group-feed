package com.dblazejewski.groups

import java.util.UUID

import akka.actor.{ Actor, ActorLogging, Props }

final case class Group(id: UUID, name: String)

final case class Groups(groups: Seq[Group])

object GroupActor {

  final case class ActionPerformed(description: String)

  final case object GetUserGroups

  def props: Props = Props[GroupActor]
}

class GroupActor extends Actor with ActorLogging {

  import GroupActor._

  var groups = Set.empty[Group]

  def receive: Receive = {
    case GetUserGroups =>
      sender() ! Groups(groups.toSeq)
  }
}
