package com.dblazejewski.application

import java.time.{LocalDateTime, ZoneOffset}
import java.util.UUID

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import com.dblazejewski.application.FeedActor.UserFeedItem
import com.dblazejewski.application.GroupFeedActor.{GetGroupFeed, GroupFeedWithUserFeedItem}
import com.dblazejewski.application.UserFeedActor.{CollectUserFeed, CollectedUserFeed}
import com.dblazejewski.support.ScalazSupport

object UserFeedActor {

  final case class CollectUserFeed(id: UUID, userId: UUID, groupActors: Seq[ActorRef], responseRef: ActorRef)

  final case class CollectedUserFeed(userId: UUID, feed: Seq[UserFeedItem], responseRef: ActorRef)

  def props(): Props = Props(new UserFeedActor())
}

class UserFeedActor() extends Actor with ActorLogging with ScalazSupport {
  private var jobId: UUID = _
  private var responses = Set.empty[Seq[UserFeedItem]]
  private var nrOfChildJobs: Long = _
  private var originalUserId: UUID = _
  private var originalSender: ActorRef = _
  private var originalResponseRef: ActorRef = _

  override def receive: Receive = {
    case CollectUserFeed(id, userId, groupActors, responseRef) =>
      jobId = id
      nrOfChildJobs = groupActors.size
      originalUserId = userId
      originalSender = sender()
      originalResponseRef = responseRef

      groupActors.foreach { groupActor =>
        groupActor ! GetGroupFeed
      }

    case GroupFeedWithUserFeedItem(feed) =>
      responses += feed
      if (responses.size == nrOfChildJobs) {
        implicit val localDateTimeOrdering: Ordering[LocalDateTime] = Ordering.by(_.toEpochSecond(ZoneOffset.UTC))
        val sortedFeed = responses.flatten.toSeq.sortBy(_.createdAt).reverse
        originalSender ! CollectedUserFeed(originalUserId, sortedFeed, originalResponseRef)
        context.stop(self)
      }
  }
}
