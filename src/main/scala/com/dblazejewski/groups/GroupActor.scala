package com.dblazejewski.groups

import akka.actor.{ Actor, ActorLogging, Props }
import com.byteslounge.slickrepo.meta.Entity

final case class Group(id: Option[Int], name: String) extends Entity[Group, Int] {
  def withId(id: Int): Group = this.copy(id = Some(id))
}

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
