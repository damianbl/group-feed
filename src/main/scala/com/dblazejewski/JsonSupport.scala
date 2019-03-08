package com.dblazejewski

import java.util.UUID

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import com.dblazejewski.groups.GroupActor.ActionPerformed
import com.dblazejewski.groups.{ Group, Groups }
import spray.json.{ DefaultJsonProtocol, JsString, JsValue, JsonFormat, RootJsonFormat }
import spray.json.deserializationError

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

  implicit val groupJsonFormat: RootJsonFormat[Group] = jsonFormat2(Group)
  implicit val groupsJsonFormat: RootJsonFormat[Groups] = jsonFormat1(Groups)

  implicit val actionPerformedJsonFormat: RootJsonFormat[ActionPerformed] = jsonFormat1(ActionPerformed)
}
