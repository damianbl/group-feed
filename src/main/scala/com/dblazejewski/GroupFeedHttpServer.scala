package com.dblazejewski

import akka.actor.{ ActorRef, ActorSystem }
import akka.event.Logging
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer
import com.dblazejewski.api.{ GroupRoutes, UserRoutes }
import com.dblazejewski.application.{ GroupActor, UserActor }
import com.dblazejewski.infrastructure.{ ConfigurationModuleImpl, PersistenceModuleImpl }
import com.typesafe.config.ConfigFactory
import slick.jdbc.meta.MTable

import scala.concurrent.duration.Duration
import scala.concurrent.{ Await, ExecutionContext, Future }
import scala.util.{ Failure, Success }
import akka.http.scaladsl.server.Directives._
import com.dblazejewski.api.support.RoutesRequestWrapper
import com.typesafe.scalalogging.StrictLogging

object GroupFeedHttpServer extends App with RoutesRequestWrapper with GroupRoutes with UserRoutes with StrictLogging {
  val conf = ConfigFactory.load("reference.conf")

  val modules = new ConfigurationModuleImpl with PersistenceModuleImpl

  implicit val system: ActorSystem = ActorSystem("group-feed-akka-http-server")
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val executionContext: ExecutionContext = system.dispatcher

  val groupActor: ActorRef = system.actorOf(
    GroupActor.props(modules.groupRepository, modules.userRepository, modules.userGroupRepository), "groupActor")
  val userActor: ActorRef = system.actorOf(UserActor.props(modules.userRepository), "userActor")

  lazy val routes: Route = requestWrapper {
    pathPrefix("api") { groupRoutes ~ userRoutes }
  }

  val serverBinding: Future[Http.ServerBinding] = Http().bindAndHandle(routes, "localhost", 8080)

  import modules.profile.api._

  modules.db.run(MTable.getTables).map { existingTables => if (existingTables.isEmpty) createSchema }

  def createSchema = {
    val schema = modules.userRepository.users.schema ++
      modules.groupRepository.groups.schema ++
      modules.postRepository.posts.schema ++
      modules.userGroupRepository.userGroups.schema

    modules.db.run(DBIO.seq(schema.create))
      .map { _ =>
        logger.info("Database schema created")
      }
      .recover {
        case t: Throwable =>
          logger.error("Database schema creation failed", t)
      }
  }

  serverBinding.onComplete {
    case Success(bound) =>
      println(s"Server online at http://${bound.localAddress.getHostString}:${bound.localAddress.getPort}/")
    case Failure(e) =>
      Console.err.println(s"Server could not start!")
      e.printStackTrace()
      system.terminate()
  }

  Await.result(system.whenTerminated, Duration.Inf)
}
