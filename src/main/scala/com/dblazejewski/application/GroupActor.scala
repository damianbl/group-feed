package com.dblazejewski.application

import akka.actor.{ Actor, ActorLogging, Props }
import com.dblazejewski.domain.{ Group, Groups }

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
