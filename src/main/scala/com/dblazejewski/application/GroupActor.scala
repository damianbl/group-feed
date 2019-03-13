package com.dblazejewski.application

import java.util.UUID

import akka.actor.{Actor, ActorLogging, Props}
import com.dblazejewski.domain.Group._
import com.dblazejewski.domain.UserGroup
import com.dblazejewski.repository.{GroupRepository, UserGroupRepository, UserRepository}
import com.dblazejewski.support.ScalazSupport
import scalaz.Scalaz._

import scala.concurrent.ExecutionContext.Implicits.global

object GroupActor {

  final case class GroupAdded(id: UUID)

  final case class GroupAddFailed(name: String)

  final case class CreateGroup(name: String)

  final case class BecomeMember(groupName: String, userId: UUID)

  final case class UserAddedToGroup(groupName: String, userId: UUID)

  final case class AddUserToGroupFailed(groupName: String, userId: UUID, msg: String)

  final case class GetUserGroups(userId: UUID)

  final case class UserGroups(userId: UUID, groupIds: Seq[UUID])

  final case class ErrorFetchingUserGroups(userId: UUID, msg: String)

  final case class UserNotFound(userId: UUID)

  def props(groupRepository: GroupRepository,
            userRepository: UserRepository,
            userGroupRepository: UserGroupRepository): Props =
    Props(new GroupActor(groupRepository, userRepository, userGroupRepository))
}

class GroupActor(groupRepository: GroupRepository,
                 userRepository: UserRepository,
                 userGroupRepository: UserGroupRepository) extends Actor with ActorLogging with ScalazSupport {

  import GroupActor._

  def receive: Receive = {
    case CreateGroup(name) => createGroup(name)
    case BecomeMember(groupName, userId) => becomeMember(groupName, userId)
    case GetUserGroups(userId) => fetchUserGroups(userId)
  }

  private def createGroup(name: String) = {
    val localSender = sender()

    groupRepository
      .add(create(name))
      .map(localSender ! GroupAdded(_))
      .recover {
        case t: Throwable =>
          localSender ! GroupAddFailed(name)
      }
  }

  private def becomeMember(groupName: String, userId: UUID) = {
    val localSender = sender()

    val result = for (
      group <- rightT(groupRepository.findByName(groupName), s"Group [$groupName] not found");
      user <- rightT(userRepository.findById(userId), s"User [$userId] not found");
      _ <- rightIf(
        userGroupRepository.isMemberOf(userId, group.id).map(!_),
        s"User [$userId] already member of group [${group.name}]");
      userAddedToGroup <- rightT(userGroupRepository.add(UserGroup(UUID.randomUUID, user.id, group.id)))
    ) yield userAddedToGroup

    result.toEither.map {
      case Right(_) =>
        localSender ! UserAddedToGroup(groupName, userId)
      case Left(error) =>
        log.error(s"Error adding user [$userId] to group [$groupName]", error.msg)
        localSender ! AddUserToGroupFailed(groupName, userId, error.msg)
    }.recover {
      case t: Throwable =>
        log.error(s"Error adding user [$userId] to group [$groupName]", t.getMessage)
        localSender ! AddUserToGroupFailed(groupName, userId, t.getMessage)
    }
  }

  private def fetchUserGroups(userId: UUID) = {
    val localSender = sender()

    (for (
      _ <- optT(userRepository.findById(userId));
      userGroups <- optT(userGroupRepository.findByUserId(userId))
    ) yield {
      localSender ! UserGroups(userId, userGroups.map(_.groupId))
    }).getOrElse(localSender ! UserNotFound(userId))

    userGroupRepository.findByUserId(userId).map { userGroups =>
      localSender ! UserGroups(userId, userGroups.map(_.groupId))
    }.recover {
      case t: Throwable =>
        log.error(s"Error fetching user [$userId] groups", t.getMessage)
        localSender ! ErrorFetchingUserGroups(userId, t.getMessage)
    }
  }

}
