package com.dblazejewski.application

import java.util.UUID

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import com.dblazejewski.application.AggregatorActor.{GetUserFeedWithResponseRef, NewGroupCreated, NewPostAdded}
import com.dblazejewski.application.FeedActor.{GetGroupFeedFailed, ReturnUserFeed}
import com.dblazejewski.application.GroupFeedActor.GetGroupFeedWithResponseRef
import com.dblazejewski.application.UserFeedActor.{CollectUserFeed, CollectedUserFeed}
import com.dblazejewski.repository.{PostRepository, UserGroupRepository}
import com.dblazejewski.support.ScalazSupport

import scala.concurrent.ExecutionContext.Implicits.global

object AggregatorActor {

  final case class NewGroupCreated(groupId: UUID)

  final case class NewPostAdded(groupId: UUID)

  final case class GetUserFeedWithResponseRef(userId: UUID, groupIds: Seq[UUID], responseRef: ActorRef)

  def props(postRepository: PostRepository, userGroupRepository: UserGroupRepository): Props =
    Props(new AggregatorActor(postRepository, userGroupRepository))
}

class AggregatorActor(postRepository: PostRepository,
                      userGroupRepository: UserGroupRepository) extends Actor with ActorLogging with ScalazSupport {

  private val groupFeedActors = Map.empty[UUID, ActorRef]

  override def preStart(): Unit = {
    userGroupRepository.findAllGroupIds().map { ids =>
      ids.foreach { id =>
        context.become(onMessage(groupFeedActors +
          (id -> context.actorOf(GroupFeedActor.props(id, postRepository), s"group-actor-$id"))))
      }
      super.preStart()
    }
  }

  def receive: Receive = onMessage(groupFeedActors)

  private def onMessage(groupFeedActors: Map[UUID, ActorRef]): Receive = {
    case NewGroupCreated(groupId) =>
      log.info(s"New group created [$groupId]")
      context.become(onMessage(groupFeedActors +
        (groupId -> context.actorOf(GroupFeedActor.props(groupId, postRepository), s"group-actor-$groupId"))))

    case getGroupFeedWithResponseRef: GetGroupFeedWithResponseRef =>
      groupFeedActors.get(getGroupFeedWithResponseRef.groupId) match {
        case Some(groupFeedActor) => groupFeedActor ! getGroupFeedWithResponseRef
        case _ =>
          val errorMsg = s"Group feed actor not found for groupId [${getGroupFeedWithResponseRef.groupId}]"
          log.warning(errorMsg)
          getGroupFeedWithResponseRef.responseRef ! GetGroupFeedFailed(getGroupFeedWithResponseRef.groupId, errorMsg)
      }

    case GetUserFeedWithResponseRef(userId, groupIds, responseRef) =>
      val id = UUID.randomUUID
      val userFeedActor = context.actorOf(UserFeedActor.props(), s"user-feed-actor-$id")
      userFeedActor ! CollectUserFeed(id, userId, groupIds.flatMap(groupFeedActors.get), responseRef)

    case CollectedUserFeed(userId, feed, responseRef) =>
      responseRef ! ReturnUserFeed(userId, feed)

    case newPostAdded: NewPostAdded =>
      log.info(s"New post added to group [${newPostAdded.groupId}]")
      groupFeedActors.get(newPostAdded.groupId).foreach(_ ! newPostAdded)
  }
}
