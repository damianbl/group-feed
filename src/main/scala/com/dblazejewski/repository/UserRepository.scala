package com.dblazejewski.repository

import com.dblazejewski.domain.User
import com.dblazejewski.infrastructure.SqlDatabase
import slick.lifted.TableQuery

class UserRepository(protected val database: SqlDatabase) extends UserSchema {

}

trait UserSchema {

  protected val database: SqlDatabase

  val user = TableQuery[UserTable]

  import database.driver.api._

  class UserTable(tag: slick.lifted.Tag) extends Table[User](tag, "USER") {
    def id = column[Int]("id", O.PrimaryKey, O.AutoInc)

    def name = column[String]("name")

    def * = (id.?, name) <> ((User.apply _).tupled, User.unapply)
  }

}
