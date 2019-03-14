package com.dblazejewski.application

import java.util.UUID

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import com.dblazejewski.application.AggregatorActor.{NewGroupCreated, NewPostAdded}
import com.dblazejewski.application.FeedActor.GetGroupFeedFailed
import com.dblazejewski.application.GroupFeedActor.GetGroupFeedWithResponseRef
import com.dblazejewski.repository.{PostRepository, UserGroupRepository}
import com.dblazejewski.support.ScalazSupport

import scala.concurrent.ExecutionContext.Implicits.global

object AggregatorActor {

  final case class NewGroupCreated(groupId: UUID)

  final case class NewPostAdded(groupId: UUID)

  def props(postRepository: PostRepository, userGroupRepository: UserGroupRepository): Props =
    Props(new AggregatorActor(postRepository, userGroupRepository))
}

class AggregatorActor(postRepository: PostRepository,
                      userGroupRepository: UserGroupRepository) extends Actor with ActorLogging with ScalazSupport {

  private var groupFeedActors = Map.empty[UUID, ActorRef]

  override def preStart(): Unit = {
    userGroupRepository.findAllGroupIds().map { ids =>
      ids.foreach { id =>
        groupFeedActors = groupFeedActors +
          (id -> context.actorOf(GroupFeedActor.props(id, postRepository), s"group-actor-$id"))
      }
      super.preStart()
    }
  }

  def receive: Receive = {
    case NewGroupCreated(groupId) =>
      groupFeedActors = groupFeedActors +
        (groupId -> context.actorOf(GroupFeedActor.props(groupId, postRepository), s"group-actor-$groupId"))
    case getGroupFeedWithResponseRef: GetGroupFeedWithResponseRef =>
      groupFeedActors.get(getGroupFeedWithResponseRef.groupId) match {
        case Some(groupFeedActor) => groupFeedActor ! getGroupFeedWithResponseRef
        case _ =>
          val errorMsg = s"Group feed actor not found for groupId [${getGroupFeedWithResponseRef.groupId}]"
          log.warning(errorMsg)
          getGroupFeedWithResponseRef.responseRef ! GetGroupFeedFailed(getGroupFeedWithResponseRef.groupId, errorMsg)
      }
    case newPostAdded: NewPostAdded =>
      log.info(s"New post added to group [${newPostAdded.groupId}]")
      groupFeedActors.get(newPostAdded.groupId).foreach(_ ! newPostAdded)
  }
}
