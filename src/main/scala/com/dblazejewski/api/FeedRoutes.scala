package com.dblazejewski.api

import java.util.UUID

import akka.actor.{ActorRef, ActorSystem}
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives.{pathPrefix, _}
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.directives.RouteDirectives.complete
import akka.pattern.ask
import akka.util.Timeout
import com.dblazejewski.application.FeedActor._
import com.dblazejewski.support.JsonSupport
import com.typesafe.scalalogging.StrictLogging

import scala.concurrent.duration._

final case class GroupFeedResponse(groupId: UUID, feed: Seq[GroupFeedItem])

final case class UserFeedResponse(userId: UUID, feed: Seq[UserFeedItem])

trait FeedRoutes extends JsonSupport with StrictLogging {

  implicit def system: ActorSystem

  def feedActor: ActorRef

  private implicit lazy val timeout: Timeout = Timeout(5 seconds)

  lazy val feedRoutes: Route =
    pathPrefix("feed") {
      pathPrefix("group" / Segment) { groupIdParam =>
        get {
          onSuccess(feedActor ? GetGroupFeed(UUID.fromString(groupIdParam))) {
            case ReturnGroupFeed(groupId, feed) =>
              complete(StatusCodes.OK, GroupFeedResponse(groupId, feed))
            case error: GetGroupFeedFailed =>
              logger.error(s"Error fetching group [${error.groupId}] feed", error.msg)
              complete(StatusCodes.InternalServerError, error)
          }
        }

      } ~
        pathPrefix("all" / Segment) { userIdParam =>
          get {
            onSuccess(feedActor ? GetUserFeed(UUID.fromString(userIdParam))) {
              case ReturnUserFeed(userId, feed) =>
                complete(StatusCodes.OK, UserFeedResponse(userId, feed))
              case error: GetUserFeedFailed =>
                logger.error(s"Error fetching user [${error.userId}] feed", error.msg)
                complete(StatusCodes.InternalServerError, error)
            }
          }
        }
    }
}
