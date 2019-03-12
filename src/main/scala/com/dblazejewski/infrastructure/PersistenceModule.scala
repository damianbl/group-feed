package com.dblazejewski.infrastructure

import com.dblazejewski.repository.{ GroupRepository, PostRepository, UserParticipatesInGroupRepository, UserRepository }
import slick.backend.DatabaseConfig
import slick.dbio.DBIO
import slick.driver.JdbcProfile

import scala.concurrent.Future

trait Profile {
  val profile: JdbcProfile
}

trait DbModule extends Profile {
  val db: JdbcProfile#Backend#Database

  implicit def executeOperation[T](databaseOperation: DBIO[T]): Future[T] = {
    db.run(databaseOperation)
  }
}

trait PersistenceModule {
  val groupRepository: GroupRepository
  val userRepository: UserRepository
  val postRepository: PostRepository
  val userParticipatesInGroupRepository: UserParticipatesInGroupRepository
}

trait PersistenceModuleImpl extends PersistenceModule with DbModule {
  this: Configuration =>

  private val dbConfig: DatabaseConfig[JdbcProfile] = DatabaseConfig.forConfig("h2db")

  override implicit val profile: JdbcProfile = dbConfig.driver
  override implicit val db: JdbcProfile#Backend#Database = dbConfig.db

  private val sqlDatabase = SqlDatabase(db, profile)

  override val groupRepository: GroupRepository = new GroupRepository(sqlDatabase)
  override val userRepository: UserRepository = new UserRepository(sqlDatabase)
  override val postRepository: PostRepository = new PostRepository(sqlDatabase)
  override val userParticipatesInGroupRepository: UserParticipatesInGroupRepository =
    new UserParticipatesInGroupRepository(sqlDatabase)
}