package com.dblazejewski.repository

import java.time.LocalDateTime

import com.dblazejewski.domain.Post
import com.dblazejewski.infrastructure.SqlDatabase
import slick.lifted.TableQuery

class PostRepository(override val database: SqlDatabase) extends PostSchema {

}

trait PostSchema extends UserSchema {
  protected val database: SqlDatabase
  val post = TableQuery[PostTable]

  import database.driver.api._

  class PostTable(tag: slick.lifted.Tag) extends Table[Post](tag, "POST") {
    def id = column[Int]("id", O.PrimaryKey, O.AutoInc)

    def authorId = column[Int]("author_id")

    def groupId = column[Int]("group_id")

    def content = column[String]("content")

    def createdAt = column[LocalDateTime]("createdAt")

    def authorFk =
      foreignKey("author_fk", authorId, user)(_.id, onDelete = ForeignKeyAction.Cascade)

    def * = (id.?, authorId, groupId, createdAt, content) <> ((Post.apply _).tupled, Post.unapply)
  }

}
