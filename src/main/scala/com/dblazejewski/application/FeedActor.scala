package com.dblazejewski.application

import java.time.LocalDateTime
import java.util.UUID

import akka.actor.{Actor, ActorLogging, Props}
import com.dblazejewski.application.FeedActor.{GetGroupFeed, GetUserFeed, UserJoinedGroup}
import com.dblazejewski.repository.{GroupRepository, PostRepository, UserGroupRepository, UserRepository}
import com.dblazejewski.support.ScalazSupport

import scala.concurrent.ExecutionContext.Implicits.global

object FeedActor {

  final case class UserJoinedGroup(userId: UUID, groupId: UUID)

  final case class GetGroupFeed(groupId: UUID)

  final case class GroupFeedItem(postId: UUID,
                                 createdAt: LocalDateTime,
                                 content: String,
                                 authorId: UUID,
                                 authorName: String)

  final case class ReturnGroupFeed(groupId: UUID, feed: Seq[GroupFeedItem])

  final case class GetGroupFeedFailed(groupId: UUID, msg: String)

  final case class GetUserFeed(userId: UUID)

  final case class UserFeedItem(postId: UUID,
                                createdAt: LocalDateTime,
                                content: String,
                                authorId: UUID,
                                authorName: String,
                                groupId: UUID)

  final case class ReturnUserFeed(userId: UUID, feed: Seq[UserFeedItem])

  final case class GetUserFeedFailed(userId: UUID, msg: String)

  def props(groupRepository: GroupRepository,
            userRepository: UserRepository,
            postRepository: PostRepository,
            userGroupRepository: UserGroupRepository): Props =
    Props(new FeedActor(groupRepository, userRepository, postRepository, userGroupRepository))

}

class FeedActor(groupRepository: GroupRepository,
                userRepository: UserRepository,
                postRepository: PostRepository,
                userGroupRepository: UserGroupRepository) extends Actor with ActorLogging with ScalazSupport {

  private var userGroupsMapping = scala.collection.mutable.Map.empty[UUID, Seq[UUID]]

  private val aggregatorActor = context.actorOf(
    AggregatorActor.props(groupRepository, userRepository, postRepository, userGroupRepository), "aggregatorActor"
  )

  override def preStart(): Unit = {
    log.info("preStart")
    userGroupRepository.findAll().map { userGroupsSeq =>
      userGroupsMapping = collection.mutable.Map(userGroupsSeq
        .groupBy(_.userId)
        .map { case (userId, userGroups) => (userId, userGroups.map(_.groupId)) }
        .toSeq: _*
      )
    }
  }

  def receive: Receive = {
    case UserJoinedGroup(userId, groupId) => handleUserJoinedGroup(userId, groupId)
    case GetGroupFeed(groupId) => getGroupFeed(groupId)
    case GetUserFeed(userId) => getUserFeed(userId)
  }

  private def handleUserJoinedGroup(userId: UUID, groupId: UUID) = {
    userGroupsMapping.get(userId) match {
      case Some(groups) =>
        log.info(s"Existing user [$userId] joined group [$groupId]")
        userGroupsMapping.put(userId, groups :+ groupId)
      case _ =>
        log.info(s"New user [$userId] joined group [$groupId]")
        userGroupsMapping.put(userId, groupId :: Nil)
    }
  }

  private def getGroupFeed(groupId: UUID) = {
    val localSender = sender()

//    aggregatorActor !

  }

  private def getUserFeed(userId: UUID) = {
    val localSender = sender()
  }
}
