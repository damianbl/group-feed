package com.dblazejewski.repository

import java.time.LocalDateTime
import java.util.UUID

import com.dblazejewski.domain.{Group, Post, PostWithAuthor, User}
import com.dblazejewski.infrastructure.SqlDatabase
import com.dblazejewski.repository.support.RepositorySupport
import slick.lifted.{ForeignKeyQuery, ProvenShape, TableQuery}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class PostRepository(override val database: SqlDatabase) extends RepositorySupport
  with UserSchema
  with PostSchema {

  import database.driver.api._

  def add(post: Post): Future[UUID] = runInDb(
    (posts returning posts.map(_.id) into ((post, id) => post.copy(id = id)) += post).map(_.id)
  )

  def findPostsWithAuthorByGroup(groupId: UUID): Future[Seq[PostWithAuthor]] = {
    val crossJoin = for {
      (p, u) <-
        (posts join users on (_.authorId === _.id))
          .filter(_._1.groupId === toByteArray(groupId))
          .sortBy(_._1.createdAt.desc)
    } yield (p.id, p.authorId, u.name, p.groupId, p.createdAt, p.content)

    runInDb(crossJoin.result.map(_.map { case (id, author_id, author_name, group_id, created_at, content) =>
      PostWithAuthor(id, author_id, author_name, group_id, created_at, content)
    }))
  }
}

trait PostSchema extends UserSchema with GroupSchema {
  protected val database: SqlDatabase
  val posts = TableQuery[PostTable]

  import database.driver.api._

  class PostTable(tag: slick.lifted.Tag) extends Table[Post](tag, "POST") {
    def id: Rep[Array[Byte]] = column[Array[Byte]]("id", O.PrimaryKey)

    def authorId: Rep[Array[Byte]] = column[Array[Byte]]("author_id")

    def groupId: Rep[Array[Byte]] = column[Array[Byte]]("group_id")

    def content: Rep[String] = column[String]("content")

    def createdAt: Rep[LocalDateTime] = column[LocalDateTime]("createdAt")

    def authorFk: ForeignKeyQuery[UserTable, User] =
      foreignKey("post_author_fk", authorId, users)(_.id, onDelete = ForeignKeyAction.Cascade)

    def groupFk: ForeignKeyQuery[GroupTable, Group] =
      foreignKey("post_group_fk", groupId, groups)(_.id, onDelete = ForeignKeyAction.Cascade)

    def * : ProvenShape[Post] = (id, authorId, groupId, createdAt, content) <>
      (nTuple =>
        Post.apply(nTuple._1, nTuple._2, nTuple._3, nTuple._4, nTuple._5),
        (p: Post) => Some(p.id, p.authorId, p.groupId, p.createdAt, p.content)
      )
  }

}
