package com.dblazejewski.api

import akka.actor.{ ActorRef, ActorSystem }
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives.{ concat, pathEnd, pathPrefix, _ }
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.directives.RouteDirectives.complete
import akka.pattern.ask
import akka.util.Timeout
import com.dblazejewski.application.GroupActor._
import com.dblazejewski.support.JsonSupport
import com.typesafe.scalalogging.StrictLogging

import scala.concurrent.duration._

final case class GroupIdAdded(id: Long)

final case class GroupNameNotAdded(name: String, msg: String)

final case class BecomeMemberOfGroupBody(groupName: String, userId: Long)

final case class AddGroupBody(name: String)

trait GroupRoutes extends JsonSupport with StrictLogging {

  implicit def system: ActorSystem

  def groupActor: ActorRef

  private implicit lazy val timeout: Timeout = Timeout(5 seconds)

  lazy val groupRoutes: Route =
    pathPrefix("group") {
      pathEnd {
        concat(
          post {
            entity(as[AddGroupBody]) { body =>
              onSuccess(groupActor ? CreateGroup(body.name)) {
                case GroupAdded(id) =>
                  complete(StatusCodes.Created, GroupIdAdded(id))
                case GroupAddFailed(name) =>
                  logger.error(s"Error adding group [$name]")
                  complete(StatusCodes.InternalServerError, GroupNameNotAdded(name, "Error adding group"))
              }
            }
          })
      } ~
        pathPrefix("user") {
          pathEnd {
            concat(
              post {
                entity(as[BecomeMemberOfGroupBody]) { body =>
                  onSuccess(groupActor ? BecomeMember(body.groupName, body.userId)) {
                    case userAdded: UserAddedToGroup =>
                      complete(StatusCodes.OK, userAdded)
                    case addUserToGroupFailed: AddUserToGroupFailed =>
                      logger.error(
                        s"""|Error adding user [${addUserToGroupFailed.userId}]
                            |to group [${addUserToGroupFailed.groupName}]""".stripMargin, addUserToGroupFailed.msg)
                      complete(StatusCodes.InternalServerError, addUserToGroupFailed)
                  }
                }
              })
          }
        }
    }
}

//pathPrefix(Segment / "user" / LongNumber) { (groupName, userId) =>
//{
//  concat(
//  post {
//  logger.info(s"Adding user [$userId] to group [$groupName]")
//  complete(StatusCodes.Created, "")
//})
//}
//}
