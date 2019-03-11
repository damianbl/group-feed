package com.dblazejewski.application

import akka.actor.{ Actor, ActorLogging, Props }
import com.dblazejewski.domain.Group._
import com.dblazejewski.domain.{ Group, Groups }
import com.dblazejewski.repository.GroupRepository

import scala.concurrent.ExecutionContext.Implicits.global
import scalaz.Scalaz._
import scalaz.OptionT.optionT

object GroupActor {

  final case class GroupAdded(id: Long)

  final case class GroupAddFailed(name: String)

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

      optionT(groupRepository.add(create(name))).map { id =>
        localSender ! GroupAdded(id)
      } getOrElse localSender ! GroupAddFailed(name)

    case GetUserGroups =>
      sender() ! Groups(groups.toSeq)
  }
}
