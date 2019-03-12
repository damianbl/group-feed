package com.dblazejewski.repository

import com.dblazejewski.domain.User
import com.dblazejewski.infrastructure.SqlDatabase
import slick.lifted.{ ProvenShape, TableQuery }

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class UserRepository(protected val database: SqlDatabase) extends UserSchema {
  def add(user: User): Future[Option[Long]] = {
    import database.driver.api._
    database.db.run((users returning users.map(_.id)
      into ((user, id) => user.copy(id = Some(id)))) += user)
  }.map(_.id)
}

trait UserSchema {

  protected val database: SqlDatabase

  val users = TableQuery[UserTable]

  import database.driver.api._

  class UserTable(tag: slick.lifted.Tag) extends Table[User](tag, "USER") {
    def id: Rep[Long] = column[Long]("id", O.PrimaryKey, O.AutoInc)

    def name: Rep[String] = column[String]("name")

    def * : ProvenShape[User] = (id.?, name) <> ((User.apply _).tupled, User.unapply)
  }

}
