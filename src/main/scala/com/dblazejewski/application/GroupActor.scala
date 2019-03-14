package com.dblazejewski.application

import java.util.UUID

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import com.dblazejewski.application.FeedActor.UserJoinedGroup
import com.dblazejewski.domain.Group._
import com.dblazejewski.domain.UserGroup
import com.dblazejewski.repository.{GroupRepository, UserGroupRepository, UserRepository}
import com.dblazejewski.support.ScalazSupport
import scalaz.Scalaz._

import scala.concurrent.ExecutionContext.Implicits.global

object GroupActor {

  final case class GroupAdded(id: UUID)

  final case class GroupAddFailed(name: String, msg: String)

  final case class CreateGroup(name: String)

  final case class BecomeMember(groupId: UUID, userId: UUID)

  final case class UserAddedToGroup(groupId: UUID, userId: UUID)

  final case class AddUserToGroupFailed(groupId: UUID, userId: UUID, msg: String)

  final case class GetUserGroups(userId: UUID)

  final case class UserGroups(userId: UUID, groupIds: Seq[UUID])

  final case class ErrorFetchingUserGroups(userId: UUID, msg: String)

  final case class UserNotFound(userId: UUID)

  def props(groupRepository: GroupRepository,
            userRepository: UserRepository,
            userGroupRepository: UserGroupRepository,
            feedActor: ActorRef): Props =
    Props(new GroupActor(groupRepository, userRepository, userGroupRepository, feedActor))
}

class GroupActor(groupRepository: GroupRepository,
                 userRepository: UserRepository,
                 userGroupRepository: UserGroupRepository,
                 feedActor: ActorRef) extends Actor with ActorLogging with ScalazSupport {

  import GroupActor._

  def receive: Receive = {
    case CreateGroup(name) => createGroup(name)
    case BecomeMember(groupId, userId) => becomeMember(groupId, userId)
    case GetUserGroups(userId) => fetchUserGroups(userId)
  }

  private def createGroup(name: String) = {
    val localSender = sender()

    groupRepository
      .add(create(name))
      .map(localSender ! GroupAdded(_))
      .recover {
        case t: Throwable =>
          log.error(s"Error adding group [$name]")
          localSender ! GroupAddFailed(name, t.getMessage)
      }
  }

  private def becomeMember(groupId: UUID, userId: UUID) = {
    val localSender = sender()

    val result = for (
      group <- rightT(groupRepository.findById(groupId), s"Group [$groupId] not found");
      user <- rightT(userRepository.findById(userId), s"User [$userId] not found");
      _ <- rightIf(
        userGroupRepository.isMemberOf(userId, group.id).map(!_),
        s"User [$userId] already member of group [${group.name}]");
      userAddedToGroup <- rightT(userGroupRepository.add(UserGroup(UUID.randomUUID, user.id, group.id)))
    ) yield userAddedToGroup

    result.toEither.map {
      case Right(_) =>
        localSender ! UserAddedToGroup(groupId, userId)
        feedActor ! UserJoinedGroup(userId, groupId)
      case Left(error) =>
        log.error(s"Error adding user [$userId] to group [$groupId]", error.msg)
        localSender ! AddUserToGroupFailed(groupId, userId, error.msg)
    }.recover {
      case t: Throwable =>
        log.error(s"Error adding user [$userId] to group [$groupId]", t.getMessage)
        localSender ! AddUserToGroupFailed(groupId, userId, t.getMessage)
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
