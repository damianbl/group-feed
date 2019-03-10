package com.dblazejewski.repository

import com.dblazejewski.domain.UserParticipatesInGroup
import com.dblazejewski.infrastructure.SqlDatabase
import slick.lifted.TableQuery

class UserParticipatesInGroupRepository(override val database: SqlDatabase) extends UserParticipatesInGroupSchema {

}

trait UserParticipatesInGroupSchema {

  protected val database: SqlDatabase
  val userParticipatesInGroup = TableQuery[UserParticipatesInGroupTable]

  import database.driver.api._

  class UserParticipatesInGroupTable(tag: slick.lifted.Tag)
    extends Table[UserParticipatesInGroup](tag, "USER_PARTICIPATES_IN_GROUP") {
    def id = column[Int]("id", O.PrimaryKey, O.AutoInc)

    def userId = column[Int]("userId")

    def groupId = column[Int]("groupId")

    def * =
      (id.?, userId, groupId) <> ((UserParticipatesInGroup.apply _).tupled, UserParticipatesInGroup.unapply)
  }

}
