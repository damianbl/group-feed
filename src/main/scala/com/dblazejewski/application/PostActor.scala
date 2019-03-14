package com.dblazejewski.application

import java.time.LocalDateTime
import java.util.UUID

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import com.dblazejewski.application.AggregatorActor.NewPostAdded
import com.dblazejewski.application.PostActor.{PostStored, StorePost, StorePostFailed}
import com.dblazejewski.domain.Post
import com.dblazejewski.repository.{GroupRepository, PostRepository, UserGroupRepository, UserRepository}
import com.dblazejewski.support.ScalazSupport
import scalaz.Scalaz._

import scala.concurrent.ExecutionContext.Implicits.global

object PostActor {

  final case class StorePost(authorId: UUID, groupId: UUID, content: String)

  final case class PostStored(postId: UUID, userId: UUID, groupId: UUID)

  final case class StorePostFailed(userId: UUID, groupId: UUID, msg: String)

  def props(groupRepository: GroupRepository,
            userRepository: UserRepository,
            postRepository: PostRepository,
            userGroupRepository: UserGroupRepository,
            aggregatorActor: ActorRef): Props =
    Props(new PostActor(groupRepository, userRepository, postRepository, userGroupRepository, aggregatorActor))

}

class PostActor(groupRepository: GroupRepository,
                userRepository: UserRepository,
                postRepository: PostRepository,
                userGroupRepository: UserGroupRepository,
                aggregatorActor: ActorRef) extends Actor with ActorLogging with ScalazSupport {

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
        aggregatorActor ! NewPostAdded(newPost.groupId)
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
