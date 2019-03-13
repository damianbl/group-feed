package com.dblazejewski.repository

import java.util.UUID

import com.dblazejewski.domain.User
import com.dblazejewski.infrastructure.SqlDatabase
import com.dblazejewski.repository.support.RepositorySupport
import slick.lifted.{ProvenShape, TableQuery}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class UserRepository(protected val database: SqlDatabase) extends UserSchema with RepositorySupport {

  import database.driver.api._

  def add(user: User): Future[UUID] = runInDb(
    (users returning users.map(_.id) into ((user, id) => user.copy(id = id)) += user).map(_.id)
  )

  def findById(id: UUID): Future[Option[User]] = runInDb(
    users.filter(_.id === toByteArray(id)).result.headOption
  )
}

trait UserSchema extends RepositorySupport {

  protected val database: SqlDatabase

  val users = TableQuery[UserTable]

  import database.driver.api._

  class UserTable(tag: slick.lifted.Tag) extends Table[User](tag, "USER") {
    def id: Rep[Array[Byte]] = column[Array[Byte]]("id", O.PrimaryKey)

    def name: Rep[String] = column[String]("name")

    def * : ProvenShape[User] =
      (id, name) <> (tuple => User.apply(tuple._1, tuple._2), (u: User) => Some((u.id, u.name)))
  }

}
