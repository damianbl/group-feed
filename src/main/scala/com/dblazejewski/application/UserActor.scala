package com.dblazejewski.application

import java.util.UUID

import akka.actor.{Actor, ActorLogging, Props}
import com.dblazejewski.domain.Group
import com.dblazejewski.domain.User._
import com.dblazejewski.repository.UserRepository

import scala.concurrent.ExecutionContext.Implicits.global

object UserActor {

  final case class UserAdded(id: UUID)

  final case class UserAddFailed(name: String)

  final case class CreateUser(name: String)

  def props(userRepository: UserRepository): Props = Props(new UserActor(userRepository))
}

class UserActor(userRepository: UserRepository) extends Actor
  with ActorLogging {

  import UserActor._

  var groups = Set.empty[Group]

  def receive: Receive = {
    case CreateUser(name) =>
      val localSender = sender()

      userRepository
        .add(create(name))
        .map(id => localSender ! UserAdded(id))
        .recover {
          case t: Throwable =>
            log.error(s"Adding user [$name] failed", t)
            localSender ! UserAddFailed(name)
        }
  }
}
