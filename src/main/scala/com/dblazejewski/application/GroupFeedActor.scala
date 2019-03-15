package com.dblazejewski.application

import java.util.UUID

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import com.dblazejewski.application.AggregatorActor.NewPostAdded
import com.dblazejewski.application.FeedActor.{ReturnGroupFeed, UserFeedItem}
import com.dblazejewski.application.GroupFeedActor.{
  GetGroupFeed, GetGroupFeedWithResponseRef,
  GroupFeedWithUserFeedItem
}
import com.dblazejewski.domain.PostWithAuthor
import com.dblazejewski.repository.PostRepository
import com.dblazejewski.support.ScalazSupport

import scala.concurrent.ExecutionContext.Implicits.global

object GroupFeedActor {

  final case class GetGroupFeedWithResponseRef(groupId: UUID, responseRef: ActorRef)

  final case class GetGroupFeed(groupId: UUID)

  final case object GetGroupFeed

  final case class GroupFeedWithUserFeedItem(feed: Seq[UserFeedItem])

  def props(groupId: UUID, postRepository: PostRepository): Props =
    Props(new GroupFeedActor(groupId, postRepository))
}

class GroupFeedActor(groupId: UUID,
                     postRepository: PostRepository) extends Actor
  with ActorLogging
  with ScalazSupport {

  private val groupFeed = Seq.empty[PostWithAuthor]

  override def preStart(): Unit = {
    postRepository.findPostsWithAuthorByGroup(groupId).map { posts =>
      context.become(onMessage(posts))
      super.preStart()
    }
  }

  override def receive: Receive = onMessage(groupFeed)

  private def onMessage(groupFeed: Seq[PostWithAuthor]): Receive = {
    case GetGroupFeedWithResponseRef(id, responseRef) => responseRef ! ReturnGroupFeed(id, groupFeed)

    case GetGroupFeed(id) => sender() ! ReturnGroupFeed(id, groupFeed)

    case GetGroupFeed =>
      sender() ! GroupFeedWithUserFeedItem(
        groupFeed.map(el => UserFeedItem(el.id, el.createdAt, el.content, el.authorId, el.authorName, groupId)))

    case NewPostAdded(group_id) =>
      postRepository.findPostsWithAuthorByGroup(group_id).map(feed => context.become(onMessage(feed)))
  }
}
