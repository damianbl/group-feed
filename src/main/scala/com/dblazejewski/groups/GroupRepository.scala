package com.dblazejewski.groups

import com.byteslounge.slickrepo.meta.Keyed
import com.byteslounge.slickrepo.repository.Repository
import slick.ast.BaseTypedType
import slick.driver.JdbcProfile

class GroupRepository(override val driver: JdbcProfile) extends Repository[Group, Int](driver) {

  import driver.api._

  val pkType = implicitly[BaseTypedType[Int]]
  val tableQuery = TableQuery[GroupsTable]
  type TableType = GroupsTable

  class GroupsTable(tag: slick.lifted.Tag) extends Table[Group](tag, "GROUPS") with Keyed[Int] {
    def id = column[Int]("id", O.PrimaryKey, O.AutoInc)

    def name = column[String]("name")

    def * = (id.?, name) <> ((Group.apply _).tupled, Group.unapply)
  }

}
