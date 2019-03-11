package com.dblazejewski.application

import akka.actor.{ Actor, ActorLogging, Props }
import com.dblazejewski.domain.Group._
import com.dblazejewski.domain.{ Group, Groups }
import com.dblazejewski.repository.GroupRepository

import scala.concurrent.ExecutionContext.Implicits.global

object GroupActor {

  final case class ActionPerformed(description: String)

  final case class CreateGroup(name: String)

  final case object GetUserGroups

  def props(groupRepository: GroupRepository): Props = Props(new GroupActor(groupRepository))
}

class GroupActor(groupRepository: GroupRepository) extends Actor with ActorLogging {

  import GroupActor._

  var groups = Set.empty[Group]

  def receive: Receive = {
    case CreateGroup(name) =>
      val localSender = sender()
      groupRepository.add(create(name)).map { rowsAdded =>
        if (rowsAdded == 1) localSender ! ActionPerformed(s"Group added [$name]")
        else localSender ! ActionPerformed(s"Error adding a group [$name]")
      }

    case GetUserGroups =>
      sender() ! Groups(groups.toSeq)
  }
}
