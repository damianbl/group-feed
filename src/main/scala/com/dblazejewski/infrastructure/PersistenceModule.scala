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
  val groupDal: GroupRepository
  val userDal: UserRepository
  val postDal: PostRepository
  val userParticipatesInGroupDal: UserParticipatesInGroupRepository
}

trait PersistenceModuleImpl extends PersistenceModule with DbModule {
  this: Configuration =>

  private val dbConfig: DatabaseConfig[JdbcProfile] = DatabaseConfig.forConfig("h2db")

  override implicit val profile: JdbcProfile = dbConfig.driver
  override implicit val db: JdbcProfile#Backend#Database = dbConfig.db

  private val sqlDatabase = SqlDatabase(db, profile)

  override val groupDal: GroupRepository = new GroupRepository(sqlDatabase)
  override val userDal: UserRepository = new UserRepository(sqlDatabase)
  override val postDal: PostRepository = new PostRepository(sqlDatabase)
  override val userParticipatesInGroupDal: UserParticipatesInGroupRepository =
    new UserParticipatesInGroupRepository(sqlDatabase)
}
