package com.dblazejewski.application

import java.time.LocalDateTime
import java.util.UUID

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import com.dblazejewski.application.AggregatorActor.GetUserFeedWithResponseRef
import com.dblazejewski.application.FeedActor.{GetGroupFeed, GetUserFeed, ReturnUserFeed, UserJoinedGroup}
import com.dblazejewski.application.GroupFeedActor.GetGroupFeedWithResponseRef
import com.dblazejewski.domain.PostWithAuthor
import com.dblazejewski.repository.{GroupRepository, PostRepository, UserGroupRepository, UserRepository}
import com.dblazejewski.support.ScalazSupport

import scala.concurrent.ExecutionContext.Implicits.global

object FeedActor {

  final case class UserJoinedGroup(userId: UUID, groupId: UUID)

  final case class GetGroupFeed(groupId: UUID)

  final case class ReturnGroupFeed(groupId: UUID, feed: Seq[PostWithAuthor])

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
            userGroupRepository: UserGroupRepository,
            aggregatorActor: ActorRef): Props =
    Props(new FeedActor(groupRepository, userRepository, postRepository, userGroupRepository, aggregatorActor))

}

class FeedActor(groupRepository: GroupRepository,
                userRepository: UserRepository,
                postRepository: PostRepository,
                userGroupRepository: UserGroupRepository,
                aggregatorActor: ActorRef) extends Actor with ActorLogging with ScalazSupport {

  private val userGroupsMapping = Map.empty[UUID, Seq[UUID]]

  override def preStart(): Unit = {
    log.info("preStart")
    userGroupRepository.findAll().map { userGroupsSeq =>
      context.become(onMessage(userGroupsSeq
        .groupBy(_.userId)
        .map { case (userId, userGroups) => (userId, userGroups.map(_.groupId)) }))

      super.preStart()
    }
  }

  def receive: Receive = onMessage(userGroupsMapping)

  private def handleUserJoinedGroup(userId: UUID, groupId: UUID): Unit = {
    userGroupsMapping.get(userId) match {
      case Some(groups) =>
        log.info(s"Existing user [$userId] joined group [$groupId]")
        context.become(onMessage(userGroupsMapping + (userId -> (groups :+ groupId))))
      case _ =>
        log.info(s"New user [$userId] joined group [$groupId]")
        context.become(onMessage(userGroupsMapping + (userId -> (groupId :: Nil))))
    }
  }

  private def onMessage(userGroupsMapping: Map[UUID, Seq[UUID]]): Receive = {
    case UserJoinedGroup(userId, groupId) => handleUserJoinedGroup(userId, groupId)

    case GetGroupFeed(groupId) => aggregatorActor ! GetGroupFeedWithResponseRef(groupId, sender())

    case GetUserFeed(userId) =>
      userGroupsMapping.get(userId) match {
        case Some(groups) => aggregatorActor ! GetUserFeedWithResponseRef(userId, groups, sender())
        case _ =>
          log.info(s"User [$userId] has not joined any groups yet")
          sender() ! ReturnUserFeed(userId, Nil)
      }
  }
}
