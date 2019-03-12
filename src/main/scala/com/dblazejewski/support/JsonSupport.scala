package com.dblazejewski.support

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import com.dblazejewski.api.{ GroupIdsResponse, _ }
import com.dblazejewski.application.GroupActor.{ AddUserToGroupFailed, ErrorFetchingUserGroups, UserAddedToGroup }
import com.dblazejewski.domain.Group
import spray.json.{ DefaultJsonProtocol, RootJsonFormat }

trait JsonSupport extends SprayJsonSupport {

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

}
