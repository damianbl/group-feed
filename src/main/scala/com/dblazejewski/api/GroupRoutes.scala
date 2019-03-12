package com.dblazejewski.api

import akka.actor.{ ActorRef, ActorSystem }
import akka.event.Logging
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives.{ concat, pathEnd, pathPrefix, _ }
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.directives.RouteDirectives.complete
import akka.pattern.ask
import akka.util.Timeout
import com.dblazejewski.JsonSupport
import com.dblazejewski.application.GroupActor.{ CreateGroup, GroupAddFailed, GroupAdded }
import com.typesafe.scalalogging.StrictLogging

import scala.concurrent.duration._

final case class GroupIdAdded(id: Long)

final case class GroupNameNotAdded(name: String, msg: String)

trait GroupRoutes extends JsonSupport with StrictLogging {

  implicit def system: ActorSystem

  def groupActor: ActorRef

  private implicit lazy val timeout: Timeout = Timeout(5 seconds)

  lazy val groupRoutes: Route =
    pathPrefix("group") {
      pathEnd {
        concat(
          post {
            entity(as[String]) { nameBody =>
              onSuccess(groupActor ? CreateGroup(nameBody)) {
                case GroupAdded(id) =>
                  complete(StatusCodes.Created, GroupIdAdded(id))
                case GroupAddFailed(name) =>
                  logger.error(s"Error adding group [$name]")
                  complete(StatusCodes.InternalServerError, GroupNameNotAdded(name, "Error adding group"))
              }
            }
          })
      }
    }
}