package com.dblazejewski.application

import akka.actor.{Actor, ActorLogging, Props}
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

  def receive: Receive = {
    case t: Test =>
      log.info("test")
  }


}
