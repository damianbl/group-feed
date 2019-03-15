package com.dblazejewski.application

import java.time.LocalDateTime
import java.util.UUID

import akka.actor.ActorSystem
import akka.testkit.{ImplicitSender, TestKit}
import com.dblazejewski.application.FeedActor.ReturnGroupFeed
import com.dblazejewski.application.GroupFeedActor.GetGroupFeed
import com.dblazejewski.domain.PostWithAuthor
import com.dblazejewski.repository.PostRepository
import org.scalamock.scalatest.MockFactory
import org.scalatest.concurrent.Eventually
import org.scalatest.{BeforeAndAfterAll, FlatSpecLike, Matchers, OneInstancePerTest}

import scala.concurrent.Future
import scala.concurrent.duration._

class GroupFeedActorSpec(_system: ActorSystem)
  extends TestKit(_system)
    with ImplicitSender
    with Matchers
    with FlatSpecLike
    with BeforeAndAfterAll
    with MockFactory
    with OneInstancePerTest
    with Eventually {

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

  "A GroupFeedActor" should "return a group feed if there are posts for the group" in {
    //given
    val groupId = UUID.randomUUID
    val getGroupFeedMessage = GetGroupFeed(groupId)
    val posts =
      PostWithAuthor(UUID.randomUUID, UUID.randomUUID, "name 1", groupId, LocalDateTime.now, "post 1") ::
        PostWithAuthor(UUID.randomUUID, UUID.randomUUID, "name 1", groupId, LocalDateTime.now, "post 2") :: Nil
    val postRepository = mock[PostRepository]
    (postRepository.findPostsWithAuthorByGroup _).expects(groupId).returning(Future.successful(posts)).anyNumberOfTimes
    val groupFeedActor = system.actorOf(GroupFeedActor.props(groupId, postRepository))

    eventually {
      //when
      groupFeedActor ! getGroupFeedMessage

      //then
      expectMsg(5 seconds, ReturnGroupFeed(groupId, posts))
    }
  }
}
