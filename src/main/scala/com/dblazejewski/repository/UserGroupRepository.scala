package com.dblazejewski.repository

import java.util.UUID

import com.dblazejewski.domain.{Group, User, UserGroup}
import com.dblazejewski.infrastructure.SqlDatabase
import com.dblazejewski.repository.support.{RepositorySupport, UuidSupport}
import slick.lifted.{ForeignKeyQuery, ProvenShape, TableQuery}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class UserGroupRepository(override val database: SqlDatabase) extends RepositorySupport
  with UserGroupSchema {

  import database.driver.api._

  def add(userGroup: UserGroup): Future[UUID] = runInDb(
    (userGroups returning userGroups.map(_.id) into ((userGroup, id) =>
      userGroup.copy(id = id)) += userGroup).map(_.id))

  def findByUserId(userId: UUID): Future[Seq[UserGroup]] = runInDb(
    userGroups.filter(_.userId === toByteArray(userId)).result
  )

  def findAll(): Future[Seq[UserGroup]] = runInDb(
    userGroups.result
  )

  def findAllGroupIds(): Future[Seq[UUID]] = runInDb(
    userGroups.map(ug => ug.groupId).groupBy(x => x).map(_._1).result.map(ids => ids.map(fromByteArray))
  )

  def isMemberOf(userId: UUID, groupId: UUID): Future[Boolean] = runInDb(
    userGroups
      .filter(ug => ug.userId === toByteArray(userId) && ug.groupId === toByteArray(groupId))
      .result
      .headOption
      .map(_.isDefined)
  )
}

trait UserGroupSchema extends UserSchema with GroupSchema with UuidSupport {

  protected val database: SqlDatabase
  val userGroups = TableQuery[UserGroupTable]

  import database.driver.api._

  class UserGroupTable(tag: slick.lifted.Tag)
    extends Table[UserGroup](tag, "USER_GROUP") {
    def id: Rep[Array[Byte]] = column[Array[Byte]]("id", O.PrimaryKey)

    def userId: Rep[Array[Byte]] = column[Array[Byte]]("userId")

    def groupId: Rep[Array[Byte]] = column[Array[Byte]]("groupId")

    def authorFk: ForeignKeyQuery[UserTable, User] =
      foreignKey("user_group_author_fk", userId, users)(_.id, onDelete = ForeignKeyAction.Cascade)

    def groupFk: ForeignKeyQuery[GroupTable, Group] =
      foreignKey("user__group_group_fk", groupId, groups)(_.id, onDelete = ForeignKeyAction.Cascade)

    def * : ProvenShape[UserGroup] = (id, userId, groupId) <>
      (nTuple =>
        UserGroup.apply(nTuple._1, nTuple._2, nTuple._3),
        (u: UserGroup) => Some((u.id, u.userId, u.groupId))
      )
  }

}
