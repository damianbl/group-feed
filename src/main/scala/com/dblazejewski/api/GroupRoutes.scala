package com.dblazejewski.api

import java.util.UUID

import akka.actor.{ActorRef, ActorSystem}
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives.{pathEnd, pathPrefix, _}
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.directives.RouteDirectives.complete
import akka.pattern.ask
import akka.util.Timeout
import com.dblazejewski.application.GroupActor._
import com.dblazejewski.support.{JsonSupport, UuidSupport}
import com.typesafe.scalalogging.StrictLogging

import scala.concurrent.duration._

final case class GroupIdAdded(id: UUID)

final case class GroupNameNotAdded(name: String, msg: String)

final case class BecomeMemberOfGroupBody(groupId: UUID, userId: UUID)

final case class AddGroupBody(name: String)

final case class GroupIdsResponse(ids: Seq[UUID])

trait GroupRoutes extends JsonSupport
  with StrictLogging {

  implicit def system: ActorSystem

  def groupActor: ActorRef

  private implicit lazy val timeout: Timeout = Timeout(5 seconds)

  lazy val groupRoutes: Route =
    pathPrefix("group") {
      pathEnd {
        post {
          entity(as[AddGroupBody]) { body =>
            onSuccess(groupActor ? CreateGroup(body.name)) {
              case GroupAdded(id) =>
                complete(StatusCodes.Created, GroupIdAdded(id))
              case GroupAddFailed(name, msg) =>
                logger.error(s"Error adding group [$name]", msg)
                complete(StatusCodes.InternalServerError, GroupNameNotAdded(name, msg))
            }
          }
        }
      } ~
        pathPrefix("user") {
          pathEnd {
            post {
              entity(as[BecomeMemberOfGroupBody]) { body =>
                onSuccess(groupActor ? BecomeMember(body.groupId, body.userId)) {
                  case userAdded: UserAddedToGroup =>
                    complete(StatusCodes.OK, userAdded)
                  case addUserToGroupFailed: AddUserToGroupFailed =>
                    logger.error(
                      s"""|Error adding user [${addUserToGroupFailed.userId}]
                          |to group [${addUserToGroupFailed.groupId}]""".stripMargin, addUserToGroupFailed.msg)
                    complete(StatusCodes.BadRequest, addUserToGroupFailed)
                }
              }
            }
          }
        } ~
        pathPrefix("user" / Segment) { userIdParam => {
          get {
            onSuccess(groupActor ? GetUserGroups(UuidSupport.getUUID(userIdParam))) {
              case UserGroups(_, groupIds) =>
                complete(StatusCodes.OK, GroupIdsResponse(groupIds))
              case error: ErrorFetchingUserGroups =>
                logger.error(s"Error fetching groups for user [${error.userId}]", error.msg)
                complete(StatusCodes.InternalServerError, error)
              case error: UserNotFound =>
                logger.error(s"User [${error.userId}] not found")
                complete(StatusCodes.NotFound, error.userId.toString)
            }
          }
        }
        }
    }
}
