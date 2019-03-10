package com.dblazejewski.repository

import com.dblazejewski.domain.Group
import com.dblazejewski.infrastructure.SqlDatabase
import slick.lifted.TableQuery

class GroupRepository(override val database: SqlDatabase) extends GroupSchema {

}

trait GroupSchema {
  protected val database: SqlDatabase
  val group = TableQuery[GroupTable]

  import database.driver.api._

  class GroupTable(tag: slick.lifted.Tag) extends Table[Group](tag, "GROUP") {
    def id = column[Int]("id", O.PrimaryKey, O.AutoInc)

    def name = column[String]("name")

    def * = (id.?, name) <> ((Group.apply _).tupled, Group.unapply)
  }

}
