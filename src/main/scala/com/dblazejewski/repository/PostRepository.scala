package com.dblazejewski.repository

import java.time.LocalDateTime

import com.dblazejewski.domain.{ Group, Post, User }
import com.dblazejewski.infrastructure.SqlDatabase
import slick.lifted.{ ForeignKeyQuery, ProvenShape, TableQuery }

class PostRepository(override val database: SqlDatabase) extends PostSchema {

}

trait PostSchema extends UserSchema with GroupSchema {
  protected val database: SqlDatabase
  val post = TableQuery[PostTable]

  import database.driver.api._

  class PostTable(tag: slick.lifted.Tag) extends Table[Post](tag, "POST") {
    def id: Rep[Int] = column[Int]("id", O.PrimaryKey, O.AutoInc)

    def authorId: Rep[Int] = column[Int]("author_id")

    def groupId: Rep[Int] = column[Int]("group_id")

    def content: Rep[String] = column[String]("content")

    def createdAt: Rep[LocalDateTime] = column[LocalDateTime]("createdAt")

    def authorFk: ForeignKeyQuery[UserTable, User] =
      foreignKey("post_author_fk", authorId, user)(_.id, onDelete = ForeignKeyAction.Cascade)

    def groupFk: ForeignKeyQuery[GroupTable, Group] =
      foreignKey("post_group_fk", groupId, groups)(_.id, onDelete = ForeignKeyAction.Cascade)

    def * : ProvenShape[Post] = (id.?, authorId, groupId, createdAt, content) <> ((Post.apply _).tupled, Post.unapply)
  }

}
