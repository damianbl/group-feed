package com.dblazejewski.application

import java.util.UUID

import akka.actor.ActorSystem
import akka.testkit.{ImplicitSender, TestKit, TestProbe}
import com.dblazejewski.application.FeedActor.ReturnGroupFeed
import com.dblazejewski.application.GroupFeedActor.GetGroupFeed
import com.dblazejewski.repository.PostRepository
import org.scalatest.{BeforeAndAfterAll, FlatSpecLike, Matchers}
import org.scalamock.scalatest.MockFactory

import scala.concurrent.Future
import scala.concurrent.duration._

class GroupFeedActorSpec(_system: ActorSystem)
  extends TestKit(_system)
    with ImplicitSender
    with Matchers
    with FlatSpecLike
    with BeforeAndAfterAll
    with MockFactory {

  def this() = this(ActorSystem("GroupFeedActorSpec"))

  override def afterAll: Unit = {
    shutdown(system)
  }

  "A GroupFeedActor" should "return empty group feed if there are no posts for the group" in {
    //given
    val groupId = UUID.randomUUID
    val getGroupFeedMessage = GetGroupFeed(groupId)
    val postRepository = mock[PostRepository]
    (postRepository.findPostsWithAuthorByGroup _).expects(groupId).returning(Future.successful(Nil))
    val groupFeedActor = system.actorOf(GroupFeedActor.props(groupId, postRepository))

    //when
    groupFeedActor ! getGroupFeedMessage

    //then
    expectMsg(5 seconds, ReturnGroupFeed(groupId, Nil))
  }
}
