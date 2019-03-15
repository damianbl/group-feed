package com.dblazejewski.api

import java.util.UUID

import akka.actor.{ActorRef, ActorSystem}
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives.{pathPrefix, _}
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.directives.RouteDirectives.complete
import akka.pattern.ask
import akka.util.Timeout
import com.dblazejewski.application.PostActor.{PostStored, StorePost, StorePostFailed}
import com.dblazejewski.support.JsonSupport
import com.typesafe.scalalogging.StrictLogging

import scala.concurrent.duration._

final case class PostBody(authorId: UUID, groupId: UUID, content: String)

final case class PostStoredResponse(id: UUID)

trait PostRoutes extends JsonSupport
  with StrictLogging {

  implicit def system: ActorSystem

  def postActor: ActorRef

  private implicit lazy val timeout: Timeout = Timeout(5 seconds)

  lazy val postRoutes: Route =
    pathPrefix("post") {
      post {
        entity(as[PostBody]) { postBody =>
          onSuccess(postActor ? StorePost(postBody.authorId, postBody.groupId, postBody.content)) {
            case PostStored(postId, _, _) =>
              complete(StatusCodes.OK, PostStoredResponse(postId))
            case error: StorePostFailed =>
              logger.error(s"Error adding post by user [${error.userId}] to group [${error.groupId}]", error.msg)
              complete(StatusCodes.InternalServerError, error)
          }
        }
      }
    }
}
