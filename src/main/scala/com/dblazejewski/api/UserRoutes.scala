package com.dblazejewski.api

import java.util.UUID

import akka.actor.{ActorRef, ActorSystem}
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives.{pathEnd, pathPrefix, _}
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.directives.RouteDirectives.complete
import akka.pattern.ask
import akka.util.Timeout
import com.dblazejewski.application.UserActor.{CreateUser, UserAddFailed, UserAdded}
import com.dblazejewski.support.JsonSupport
import com.typesafe.scalalogging.StrictLogging

import scala.concurrent.duration._

final case class UserIdAdded(id: UUID)

final case class UserNameNotAdded(name: String, msg: String)

final case class AddUserBody(name: String)

trait UserRoutes extends JsonSupport with StrictLogging {

  implicit def system: ActorSystem

  def userActor: ActorRef

  private implicit lazy val timeout: Timeout = Timeout(5 seconds)

  lazy val userRoutes: Route =
    pathPrefix("user") {
      pathEnd {
        post {
          entity(as[AddUserBody]) { body =>
            onSuccess(userActor ? CreateUser(body.name)) {
              case UserAdded(id) =>
                complete(StatusCodes.Created, UserIdAdded(id))
              case UserAddFailed(name) =>
                logger.error(s"Error adding user [$name]")
                complete(StatusCodes.InternalServerError, UserNameNotAdded(name, "Error adding user"))
            }
          }
        }
      }
    }
}
