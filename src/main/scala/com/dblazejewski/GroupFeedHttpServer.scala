package com.dblazejewski

import akka.actor.{ActorRef, ActorSystem}
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer
import com.dblazejewski.api.support.RoutesRequestWrapper
import com.dblazejewski.api.{FeedRoutes, GroupRoutes, PostRoutes, UserRoutes}
import com.dblazejewski.application._
import com.dblazejewski.infrastructure.{ConfigurationModuleImpl, PersistenceModuleImpl}
import com.typesafe.config.ConfigFactory
import com.typesafe.scalalogging.StrictLogging

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.util.{Failure, Success}

object GroupFeedHttpServer extends App
  with RoutesRequestWrapper
  with GroupRoutes
  with UserRoutes
  with PostRoutes
  with FeedRoutes
  with StrictLogging {

  val conf = ConfigFactory.load("reference.conf")

  val modules = new ConfigurationModuleImpl with PersistenceModuleImpl

  implicit val system: ActorSystem = ActorSystem("group-feed-akka-http-server")
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val executionContext: ExecutionContext = system.dispatcher

  val aggregatorActor: ActorRef = system.actorOf(
    AggregatorActor.props(modules.postRepository, modules.userGroupRepository), "aggregatorActor"
  )

  val feedActor: ActorRef = system.actorOf(
    FeedActor.props(
      modules.groupRepository,
      modules.userRepository,
      modules.postRepository,
      modules.userGroupRepository,
      aggregatorActor), "feedActor")

  val groupActor: ActorRef = system.actorOf(
    GroupActor.props(
      modules.groupRepository,
      modules.userRepository,
      modules.userGroupRepository,
      feedActor,
      aggregatorActor), "groupActor")

  val userActor: ActorRef = system.actorOf(UserActor.props(modules.userRepository), "userActor")

  val postActor: ActorRef = system.actorOf(
    PostActor.props(
      modules.groupRepository,
      modules.userRepository,
      modules.postRepository,
      modules.userGroupRepository,
      aggregatorActor), "postActor")

  lazy val routes: Route = requestWrapper {
    pathPrefix("api") {groupRoutes ~ userRoutes ~ postRoutes ~ feedRoutes}
  }

  val serverBinding: Future[Http.ServerBinding] = Http().bindAndHandle(routes, "localhost", 8080)

  import modules.profile.api._

  val schema = modules.userRepository.users.schema ++
    modules.groupRepository.groups.schema ++
    modules.postRepository.posts.schema ++
    modules.userGroupRepository.userGroups.schema

  modules.db.run(DBIO.seq(schema.createIfNotExists))
    .map { _ => logger.info("Database schema created if not existed") }
    .recover {
      case t: Throwable =>
        logger.error("Database schema creation failed", t)
    }

  serverBinding.onComplete {
    case Success(bound) =>
      logger.info(s"Server online at http://${bound.localAddress.getHostString}:${bound.localAddress.getPort}/")
    case Failure(e) =>
      logger.error("Server could not start!", e)
      system.terminate()
  }

  Await.result(system.whenTerminated, Duration.Inf)
}
