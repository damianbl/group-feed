package com.dblazejewski.application

import java.util.UUID

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import com.dblazejewski.application.AggregatorActor.NewPostAdded
import com.dblazejewski.application.FeedActor.ReturnGroupFeed
import com.dblazejewski.application.GroupFeedActor.GetGroupFeedWithResponseRef
import com.dblazejewski.domain.PostWithAuthor
import com.dblazejewski.repository.PostRepository
import com.dblazejewski.support.ScalazSupport

import scala.concurrent.ExecutionContext.Implicits.global

object GroupFeedActor {

  final case class GetGroupFeedWithResponseRef(groupId: UUID, responseRef: ActorRef)

  def props(groupId: UUID, postRepository: PostRepository): Props =
    Props(new GroupFeedActor(groupId, postRepository))
}

class GroupFeedActor(groupId: UUID,
                     postRepository: PostRepository) extends Actor with ActorLogging with ScalazSupport {

  private var groupFeed = Seq.empty[PostWithAuthor]

  override def preStart(): Unit = {
    postRepository.findPostWithAuthorByGroup(groupId).map { posts =>
      groupFeed = posts
      super.preStart()
    }
  }

  override def receive: Receive = {
    case GetGroupFeedWithResponseRef(id, responseRef) =>
      responseRef ! ReturnGroupFeed(id, groupFeed)
    case NewPostAdded(groupId) => postRepository.findPostWithAuthorByGroup(groupId).map { posts =>
      groupFeed = posts
    }
  }
}
