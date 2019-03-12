package com.dblazejewski.api

import akka.actor.{ActorRef, ActorSystem}
import akka.event.Logging
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives.{concat, pathEnd, pathPrefix, _}
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.directives.RouteDirectives.complete
import akka.pattern.ask
import akka.util.Timeout
import com.dblazejewski.application.UserActor.{CreateUser, UserAddFailed, UserAdded}
import com.dblazejewski.support.JsonSupport
import com.typesafe.scalalogging.StrictLogging

import scala.concurrent.duration._

final case class UserIdAdded(id: Long)

final case class UserNameNotAdded(name: String, msg: String)

trait UserRoutes extends JsonSupport with StrictLogging {

  implicit def system: ActorSystem

  def userActor: ActorRef

  private implicit lazy val timeout: Timeout = Timeout(5 seconds)

  lazy val userRoutes: Route =
    pathPrefix("user") {
      pathEnd {
        concat(
          post {
            entity(as[String]) { nameBody =>
              onSuccess(userActor ? CreateUser(nameBody)) {
                case UserAdded(id) =>
                  complete(StatusCodes.Created, UserIdAdded(id))
                case UserAddFailed(name) =>
                  logger.error(s"Error adding user [$name]")
                  complete(StatusCodes.InternalServerError, UserNameNotAdded(name, "Error adding user"))
              }
            }
          })
      }
    }
}
