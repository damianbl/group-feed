package com.dblazejewski.support

import java.time.LocalDateTime
import java.util.UUID

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import com.dblazejewski.api.{GroupFeedResponse, GroupIdsResponse, PostStoredResponse, UserFeedResponse, _}
import com.dblazejewski.application.FeedActor._
import com.dblazejewski.application.GroupActor.{AddUserToGroupFailed, ErrorFetchingUserGroups, UserAddedToGroup}
import com.dblazejewski.application.PostActor.StorePostFailed
import com.dblazejewski.domain.{Group, PostWithAuthor}
import spray.json.{DefaultJsonProtocol, DeserializationException, JsString, JsValue, JsonFormat, RootJsonFormat}

trait JsonSupport extends SprayJsonSupport {

  implicit object UUIDFormat extends JsonFormat[UUID] {
    def write(uuid: UUID): JsValue = JsString(uuid.toString)

    def read(value: JsValue): UUID =
      value match {
        case JsString(uuid) => UUID.fromString(uuid)
        case _ => throw DeserializationException("Expected hexadecimal UUID string")
      }
  }

  implicit object LocalDateTimeFormat extends JsonFormat[LocalDateTime] {
    def write(localDateTime: LocalDateTime): JsValue = JsString(localDateTime.toString)

    def read(value: JsValue): LocalDateTime =
      value match {
        case JsString(localDateTime) => LocalDateTime.parse(localDateTime)
        case _ => throw DeserializationException("Expected LocalDateTime string")
      }
  }

  import DefaultJsonProtocol._

  implicit val groupJsonFormat: RootJsonFormat[Group] = jsonFormat2(Group.apply)

  implicit val groupIdAddedHttpResponse: RootJsonFormat[GroupIdAdded] = jsonFormat1(GroupIdAdded.apply)
  implicit val groupNameNotAddedHttpResponse: RootJsonFormat[GroupNameNotAdded] = jsonFormat2(GroupNameNotAdded.apply)

  implicit val userIdAddedHttpResponse: RootJsonFormat[UserIdAdded] = jsonFormat1(UserIdAdded.apply)
  implicit val userNameNotAddedHttpResponse: RootJsonFormat[UserNameNotAdded] = jsonFormat2(UserNameNotAdded.apply)
  implicit val userAddedToGroupHttpResponse: RootJsonFormat[UserAddedToGroup] = jsonFormat2(UserAddedToGroup.apply)
  implicit val addUserToGroupFailedHttpResponse: RootJsonFormat[AddUserToGroupFailed] =
    jsonFormat3(AddUserToGroupFailed.apply)
  implicit val becomeMemberOfGroupBody: RootJsonFormat[BecomeMemberOfGroupBody] =
    jsonFormat2(BecomeMemberOfGroupBody.apply)

  implicit val addUserBody: RootJsonFormat[AddUserBody] = jsonFormat1(AddUserBody.apply)
  implicit val addGroupBody: RootJsonFormat[AddGroupBody] = jsonFormat1(AddGroupBody.apply)
  implicit val groupIdsResponse: RootJsonFormat[GroupIdsResponse] = jsonFormat1(GroupIdsResponse.apply)
  implicit val errorFetchingUserGroups: RootJsonFormat[ErrorFetchingUserGroups] =
    jsonFormat2(ErrorFetchingUserGroups.apply)

  implicit val storePostFailed: RootJsonFormat[StorePostFailed] = jsonFormat3(StorePostFailed.apply)
  implicit val postBody: RootJsonFormat[PostBody] = jsonFormat3(PostBody.apply)
  implicit val postStoredResponse: RootJsonFormat[PostStoredResponse] = jsonFormat1(PostStoredResponse.apply)

  implicit val postWithAuthor: RootJsonFormat[PostWithAuthor] = jsonFormat6(PostWithAuthor.apply)
  implicit val userFeedItem: RootJsonFormat[UserFeedItem] = jsonFormat6(UserFeedItem.apply)
  implicit val groupFeedResponse: RootJsonFormat[GroupFeedResponse] = jsonFormat2(GroupFeedResponse.apply)
  implicit val getGroupFeedFailed: RootJsonFormat[GetGroupFeedFailed] = jsonFormat2(GetGroupFeedFailed.apply)
  implicit val userFeedResponse: RootJsonFormat[UserFeedResponse] = jsonFormat2(UserFeedResponse.apply)
  implicit val getUserFeedFailed: RootJsonFormat[GetUserFeedFailed] = jsonFormat2(GetUserFeedFailed.apply)

}
