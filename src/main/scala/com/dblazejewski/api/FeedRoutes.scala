package com.dblazejewski.api

import java.util.UUID

import akka.actor.{ActorRef, ActorSystem}
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives.{pathPrefix, _}
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.directives.RouteDirectives.complete
import akka.pattern.ask
import akka.util.Timeout
import com.dblazejewski.api.support.AuthorizationSupport
import com.dblazejewski.application.FeedActor._
import com.dblazejewski.domain.PostWithAuthor
import com.dblazejewski.infrastructure.ConfigurationModuleImpl
import com.dblazejewski.support.JsonSupport
import com.typesafe.scalalogging.StrictLogging
import com.dblazejewski.support.UuidSupport._

import scala.concurrent.duration._

final case class GroupFeedResponse(groupId: UUID, feed: Seq[PostWithAuthor])

final case class UserFeedResponse(userId: UUID, feed: Seq[UserFeedItem])

trait FeedRoutes extends JsonSupport
  with AuthorizationSupport
  with StrictLogging {

  implicit def system: ActorSystem

  def feedActor: ActorRef

  private implicit lazy val timeout: Timeout = Timeout(5 seconds)

  lazy val feedRoutes: Route =
    extractCredentials { credentials =>
      authorizeAsync(_ => hasValidToken(tokenAuthenticator(credentials))) {
        pathPrefix("feed") {
          pathPrefix("group" / Segment) { groupIdParam =>
            get {
              onSuccess(feedActor ? GetGroupFeed(getUUID(groupIdParam))) {
                case ReturnGroupFeed(groupId, feed) =>
                  complete(StatusCodes.OK, GroupFeedResponse(groupId, feed))
                case error: GetGroupFeedFailed =>
                  logger.error(s"Error fetching group [${error.groupId}] feed", error.msg)
                  complete(StatusCodes.NotFound, error)
              }
            }

          } ~
            pathPrefix("all" / Segment) { userIdParam =>
              get {
                onSuccess(feedActor ? GetUserFeed(getUUID(userIdParam))) {
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
    }
}
