package com.dblazejewski.repository

import java.time.LocalDateTime

import com.dblazejewski.domain.{Group, Post, User}
import com.dblazejewski.infrastructure.SqlDatabase
import com.dblazejewski.repository.support.RepositorySupport
import slick.lifted.{ForeignKeyQuery, ProvenShape, TableQuery}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class PostRepository(override val database: SqlDatabase) extends PostSchema with RepositorySupport {

  import database.driver.api._

  def add(post: Post): Future[Option[Long]] = runInDb(
    (posts returning posts.map(_.id) into ((post, id) => post.copy(id = Some(id))) += post).map(_.id)
  )
}

trait PostSchema extends UserSchema with GroupSchema {
  protected val database: SqlDatabase
  val posts = TableQuery[PostTable]

  import database.driver.api._

  class PostTable(tag: slick.lifted.Tag) extends Table[Post](tag, "POST") {
    def id: Rep[Long] = column[Long]("id", O.PrimaryKey, O.AutoInc)

    def authorId: Rep[Long] = column[Long]("author_id")

    def groupId: Rep[Long] = column[Long]("group_id")

    def content: Rep[String] = column[String]("content")

    def createdAt: Rep[LocalDateTime] = column[LocalDateTime]("createdAt")

    def authorFk: ForeignKeyQuery[UserTable, User] =
      foreignKey("post_author_fk", authorId, users)(_.id, onDelete = ForeignKeyAction.Cascade)

    def groupFk: ForeignKeyQuery[GroupTable, Group] =
      foreignKey("post_group_fk", groupId, groups)(_.id, onDelete = ForeignKeyAction.Cascade)

    def * : ProvenShape[Post] = (id.?, authorId, groupId, createdAt, content) <> ((Post.apply _).tupled, Post.unapply)
  }

}
