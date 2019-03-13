package com.dblazejewski.application

import java.time.LocalDateTime
import java.util.UUID

import akka.actor.{Actor, ActorLogging, Props}
import com.dblazejewski.application.PostActor.{PostStored, StorePost, StorePostFailed}
import com.dblazejewski.domain.Post
import com.dblazejewski.repository.{GroupRepository, PostRepository, UserGroupRepository, UserRepository}
import com.dblazejewski.support.ScalazSupport
import scalaz.Scalaz._

import scala.concurrent.ExecutionContext.Implicits.global

object FeedActor {

  final case class GetGroupFeed(groupId: UUID)

  final case class FeedItem(authorName: String, content: String, createdAt: LocalDateTime)

  final case class ReturnGroupFeed(groupId: UUID, feed: Seq[FeedItem])

  final case class GetGroupFeedFailed(groupId: UUID, msg: String)

  final case class GetUserFeed(userId: UUID)

  final case class ReturnUserFeed(userId: UUID, feed: Seq[FeedItem])

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

  def receive: Receive = {
    case StorePost(authorId, groupId, content) => storePost(authorId, groupId, content)
  }

  private def storePost(authorId: UUID, groupId: UUID, content: String) = {
    val localSender = sender()

    val newPost = Post(UUID.randomUUID, authorId, groupId, LocalDateTime.now, content)

    val result = for (
      group <- rightT(groupRepository.findById(newPost.groupId), s"Group [${newPost.id}] not found");
      user <- rightT(userRepository.findById(newPost.authorId), s"User [${newPost.authorId}] not found");
      _ <- rightIf(
        userGroupRepository.isMemberOf(user.id, group.id),
        s"User [${user.id}] is not a member of group [${group.name}]");
      postStored <- rightT(postRepository.add(newPost))
    ) yield postStored

    result.toEither.map {
      case Right(postId) =>
        localSender ! PostStored(postId, newPost.authorId, newPost.groupId)
      case Left(error) =>
        log.error(s"Error storing post for user [${newPost.authorId}] to group [${newPost.groupId}]", error.msg)
        localSender ! StorePostFailed(newPost.authorId, newPost.groupId, error.msg)
    }.recover {
      case t: Throwable =>
        log.error(s"Error storing post for user [${newPost.authorId}] to group [${newPost.groupId}]", t.getMessage)
        localSender ! StorePostFailed(newPost.authorId, newPost.groupId, t.getMessage)
    }

  }
}
