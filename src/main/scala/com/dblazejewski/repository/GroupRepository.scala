package com.dblazejewski.repository

import java.util.UUID

import com.dblazejewski.domain.Group
import com.dblazejewski.infrastructure.SqlDatabase
import com.dblazejewski.repository.support.{RepositorySupport, UuidSupport}
import slick.lifted.{ProvenShape, TableQuery}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class GroupRepository(override val database: SqlDatabase) extends GroupSchema with RepositorySupport {

  import database.driver.api._

  def add(group: Group): Future[UUID] = runInDb(
    (groups returning groups.map(_.id) into ((group, id) => group.copy(id = id)) += group).map(_.id)
  )

  def findByName(name: String): Future[Option[Group]] = runInDb(
    groups.filter(_.name.toLowerCase === name.toLowerCase).result.headOption
  )
}

trait GroupSchema extends UuidSupport {
  protected val database: SqlDatabase
  val groups = TableQuery[GroupTable]

  import database.driver.api._

  class GroupTable(tag: slick.lifted.Tag) extends Table[Group](tag, "GROUP") {
    def id: Rep[Array[Byte]] = column[Array[Byte]]("id", O.PrimaryKey)

    def name: Rep[String] = column[String]("name", O.Unique)

    def * : ProvenShape[Group] =
      (id, name) <> (tuple => Group.apply(tuple._1, tuple._2), (g: Group) => Some((g.id, g.name)))
  }

}
