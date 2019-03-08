package com.dblazejewski.groups

import akka.actor.{ ActorRef, ActorSystem }
import akka.event.Logging
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.directives.MethodDirectives.get
import akka.http.scaladsl.server.directives.RouteDirectives.complete
import akka.pattern.ask
import akka.util.Timeout
import com.dblazejewski.JsonSupport
import com.dblazejewski.groups.GroupActor._

import scala.concurrent.Future
import scala.concurrent.duration._

trait GroupRoutes extends JsonSupport {

  implicit def system: ActorSystem

  lazy val log = Logging(system, classOf[GroupRoutes])

  def groupActor: ActorRef

  implicit lazy val timeout: Timeout = Timeout(5.seconds)

  lazy val groupRoutes: Route =
    pathPrefix("groups") {
      pathEnd {
        concat(
          get {
            val groups: Future[Groups] =
              (groupActor ? GetUserGroups).mapTo[Groups]
            complete(groups)
          })
      }
    }
}
