package com.dblazejewski.application

import java.util.UUID

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import com.dblazejewski.application.AggregatorActor.Test
import com.dblazejewski.repository.{GroupRepository, PostRepository, UserGroupRepository, UserRepository}
import com.dblazejewski.support.ScalazSupport

object AggregatorActor {

  final case class Test()

  def props(groupRepository: GroupRepository,
            userRepository: UserRepository,
            postRepository: PostRepository,
            userGroupRepository: UserGroupRepository): Props =
    Props(new AggregatorActor(groupRepository, userRepository, postRepository, userGroupRepository))
}

class AggregatorActor(groupRepository: GroupRepository,
                      userRepository: UserRepository,
                      postRepository: PostRepository,
                      userGroupRepository: UserGroupRepository) extends Actor with ActorLogging with ScalazSupport {

  private var groupActors = scala.collection.mutable.Map[UUID, ActorRef]

  def receive: Receive = {
    case t: Test =>
      log.info("test")
  }


}