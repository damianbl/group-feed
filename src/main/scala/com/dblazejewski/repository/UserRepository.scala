package com.dblazejewski.repository

import com.dblazejewski.domain.User
import com.dblazejewski.infrastructure.SqlDatabase
import slick.lifted.{ ProvenShape, TableQuery }

class UserRepository(protected val database: SqlDatabase) extends UserSchema {

}

trait UserSchema {

  protected val database: SqlDatabase

  val user = TableQuery[UserTable]

  import database.driver.api._

  class UserTable(tag: slick.lifted.Tag) extends Table[User](tag, "USER") {
    def id: Rep[Int] = column[Int]("id", O.PrimaryKey, O.AutoInc)

    def name: Rep[String] = column[String]("name")

    def * : ProvenShape[User] = (id.?, name) <> ((User.apply _).tupled, User.unapply)
  }

}
