package com.dblazejewski.application

import akka.actor.{ Actor, ActorLogging, Props }
import com.dblazejewski.domain.Group._
import com.dblazejewski.domain.{ Group, UserGroup }
import com.dblazejewski.repository.{ GroupRepository, UserGroupRepository, UserRepository }
import com.dblazejewski.support.ScalazSupport
import scalaz.EitherT
import scalaz.OptionT.optionT
import scalaz.Scalaz._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object GroupActor {

  final case class GroupAdded(id: Long)

  final case class GroupAddFailed(name: String)

  final case class CreateGroup(name: String)

  final case class BecomeMember(groupName: String, userId: Long)

  final case class UserAddedToGroup(groupName: String, userId: Long)

  final case class AddUserToGroupFailed(groupName: String, userId: Long, msg: String)

  final case class GetUserGroups(userId: Long)

  final case class UserGroups(userId: Long, groupIds: Seq[Long])

  final case class ErrorFetchingUserGroups(userId: Long, msg: String)

  def props(
    groupRepository: GroupRepository,
    userRepository: UserRepository,
    userGroupRepository: UserGroupRepository): Props =
    Props(new GroupActor(groupRepository, userRepository, userGroupRepository))
}

class GroupActor(
  groupRepository: GroupRepository,
  userRepository: UserRepository,
  userGroupRepository: UserGroupRepository) extends Actor with ActorLogging with ScalazSupport {

  import GroupActor._

  var groups = Set.empty[Group]

  def receive: Receive = {
    case CreateGroup(name) => createGroup(name)
    case BecomeMember(groupName, userId) => becomeMember(groupName, userId)
    case GetUserGroups(userId) => fetchUserGroups(userId)
  }

  private def createGroup(name: String) = {
    val localSender = sender()

    optionT(groupRepository.add(create(name))).map { id =>
      localSender ! GroupAdded(id)
    } getOrElse localSender ! GroupAddFailed(name)
  }

  private def becomeMember(groupName: String, userId: Long) = {
    val localSender = sender()

    val result: EitherT[Future, ScalazSupport.Error, Option[Long]] = for (
      group <- rightT(groupRepository.findByName(groupName), s"Group [$groupName] not found");
      user <- rightT(userRepository.findById(userId), s"User [$userId] not found");
      _ <- rightIf(
        userGroupRepository.isMemberOf(userId, group.id.get).map(!_),
        s"User [$userId] already member of group [${group.name}]");
      userAddedToGroup <- rightT(userGroupRepository.add(UserGroup(None, user.id.get, group.id.get)))
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

  private def fetchUserGroups(userId: Long) = {
    val localSender = sender()

    userGroupRepository.findByUserId(userId).map { userGroups =>
      localSender ! UserGroups(userId, userGroups.map(_.groupId))
    }.recover {
      case t: Throwable =>
        log.error(s"Error fetching user [$userId] groups", t.getMessage)
        localSender ! ErrorFetchingUserGroups(userId, t.getMessage)
    }
  }

}
