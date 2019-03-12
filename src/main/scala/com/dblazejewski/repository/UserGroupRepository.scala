package com.dblazejewski.repository

import com.dblazejewski.domain.{ Group, User, UserGroup }
import com.dblazejewski.infrastructure.SqlDatabase
import com.dblazejewski.repository.support.RepositorySupport
import slick.lifted.{ ForeignKeyQuery, ProvenShape, TableQuery }

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class UserGroupRepository(override val database: SqlDatabase) extends UserGroupSchema with RepositorySupport {

  import database.driver.api._

  def add(userGroup: UserGroup): Future[Option[Long]] = runInDb(
    (userGroups returning userGroups.map(_.id) into ((userGroup, id) =>
      userGroup.copy(id = Some(id))) += userGroup).map(_.id))

  def findByUserId(userId: Long): Future[Seq[UserGroup]] = runInDb(
    userGroups.filter(_.userId === userId).result)

  def isMemberOf(userId: Long, groupId: Long): Future[Boolean] = runInDb(
    userGroups.filter(ug => ug.userId === userId && ug.groupId === groupId).result.headOption.map(_.isDefined))
}

trait UserGroupSchema extends UserSchema with GroupSchema {

  protected val database: SqlDatabase
  val userGroups = TableQuery[UserGroupTable]

  import database.driver.api._

  class UserGroupTable(tag: slick.lifted.Tag)
    extends Table[UserGroup](tag, "USER_GROUP") {
    def id: Rep[Long] = column[Long]("id", O.PrimaryKey, O.AutoInc)

    def userId: Rep[Long] = column[Long]("userId")

    def groupId: Rep[Long] = column[Long]("groupId")

    def authorFk: ForeignKeyQuery[UserTable, User] =
      foreignKey("user_group_author_fk", userId, users)(_.id, onDelete = ForeignKeyAction.Cascade)

    def groupFk: ForeignKeyQuery[GroupTable, Group] =
      foreignKey("user__group_group_fk", groupId, groups)(_.id, onDelete = ForeignKeyAction.Cascade)

    def * : ProvenShape[UserGroup] =
      (id.?, userId, groupId) <> ((UserGroup.apply _).tupled, UserGroup.unapply)
  }

}
