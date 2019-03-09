package com.dblazejewski

import akka.actor.{ ActorRef, ActorSystem }
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer
import com.dblazejewski.groups.{ GroupActor, GroupRoutes }
import com.dblazejewski.infrastructure.{ ConfigurationModuleImpl, PersistenceModuleImpl }
import com.typesafe.config.ConfigFactory

import scala.concurrent.duration.Duration
import scala.concurrent.{ Await, ExecutionContext, Future }
import scala.util.{ Failure, Success }

object GroupFeedHttpServer extends App with GroupRoutes {
  val conf = ConfigFactory.load("reference.conf")

  val modules = new ConfigurationModuleImpl with PersistenceModuleImpl

  implicit val system: ActorSystem = ActorSystem("group-feed-akka-http-server")
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val executionContext: ExecutionContext = system.dispatcher

  val groupActor: ActorRef = system.actorOf(GroupActor.props, "groupActor")

  lazy val routes: Route = groupRoutes
  val serverBinding: Future[Http.ServerBinding] = Http().bindAndHandle(routes, "localhost", 8080)

  import modules.profile.api._

  Await.result(modules.db.run(modules.groupDal.tableQuery.schema.createIfNotExists), Duration.Inf)

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
