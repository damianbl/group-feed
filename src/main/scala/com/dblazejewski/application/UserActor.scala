package com.dblazejewski.application

import akka.actor.{ Actor, ActorLogging, Props }
import com.dblazejewski.domain.Group
import com.dblazejewski.domain.User._
import com.dblazejewski.repository.UserRepository
import scalaz.OptionT.optionT
import scalaz.Scalaz._

import scala.concurrent.ExecutionContext.Implicits.global

object UserActor {

  final case class UserAdded(id: Long)

  final case class UserAddFailed(name: String)

  final case class CreateUser(name: String)

  def props(userRepository: UserRepository): Props = Props(new UserActor(userRepository))
}

class UserActor(userRepository: UserRepository) extends Actor with ActorLogging {

  import UserActor._

  var groups = Set.empty[Group]

  def receive: Receive = {
    case CreateUser(name) =>
      val localSender = sender()

      optionT(userRepository.add(create(name))).map { id =>
        localSender ! UserAdded(id)
      } getOrElse localSender ! UserAddFailed(name)
  }
}
