package com.dblazejewski.repository

import com.dblazejewski.domain.{ Group, User, UserParticipatesInGroup }
import com.dblazejewski.infrastructure.SqlDatabase
import slick.lifted.{ ForeignKeyQuery, ProvenShape, TableQuery }

class UserParticipatesInGroupRepository(override val database: SqlDatabase) extends UserParticipatesInGroupSchema {

}

trait UserParticipatesInGroupSchema extends UserSchema with GroupSchema {

  protected val database: SqlDatabase
  val userParticipatesInGroup = TableQuery[UserParticipatesInGroupTable]

  import database.driver.api._

  class UserParticipatesInGroupTable(tag: slick.lifted.Tag)
    extends Table[UserParticipatesInGroup](tag, "USER_PARTICIPATES_IN_GROUP") {
    def id: Rep[Int] = column[Int]("id", O.PrimaryKey, O.AutoInc)

    def userId: Rep[Int] = column[Int]("userId")

    def groupId: Rep[Int] = column[Int]("groupId")

    def authorFk: ForeignKeyQuery[UserTable, User] =
      foreignKey("user_participates_in_group_author_fk", userId, user)(_.id, onDelete = ForeignKeyAction.Cascade)

    def groupFk: ForeignKeyQuery[GroupTable, Group] =
      foreignKey("user_participates_in_group_group_fk", groupId, group)(_.id, onDelete = ForeignKeyAction.Cascade)

    def * : ProvenShape[UserParticipatesInGroup] =
      (id.?, userId, groupId) <> ((UserParticipatesInGroup.apply _).tupled, UserParticipatesInGroup.unapply)
  }

}
