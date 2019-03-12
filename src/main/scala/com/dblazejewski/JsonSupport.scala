package com.dblazejewski

import java.util.UUID

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import com.dblazejewski.api.{ GroupIdAdded, GroupNameNotAdded, UserIdAdded, UserNameNotAdded }
import com.dblazejewski.domain.{ Group, Groups }
import spray.json.{ DefaultJsonProtocol, JsString, JsValue, JsonFormat, RootJsonFormat, deserializationError }

trait UuidMarshalling {

  implicit object UuidJsonFormat extends JsonFormat[UUID] {
    def write(x: UUID) = JsString(x toString ())

    def read(value: JsValue): UUID = value match {
      case JsString(x) => UUID.fromString(x)
      case x => deserializationError(s"Expected UUID as JsString, but got $x")
    }
  }

}

trait JsonSupport extends SprayJsonSupport with UuidMarshalling {

  import DefaultJsonProtocol._

  implicit val groupJsonFormat: RootJsonFormat[Group] = jsonFormat2(Group.apply)
  implicit val groupsJsonFormat: RootJsonFormat[Groups] = jsonFormat1(Groups)

  implicit val groupIdAddedHttpResponse: RootJsonFormat[GroupIdAdded] = jsonFormat1(GroupIdAdded.apply)
  implicit val groupNameNotAddedHttpResponse: RootJsonFormat[GroupNameNotAdded] = jsonFormat2(GroupNameNotAdded.apply)

  implicit val userIdAddedHttpResponse: RootJsonFormat[UserIdAdded] = jsonFormat1(UserIdAdded.apply)
  implicit val userNameNotAddedHttpResponse: RootJsonFormat[UserNameNotAdded] = jsonFormat2(UserNameNotAdded.apply)

}
