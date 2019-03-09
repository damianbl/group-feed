package com.dblazejewski.infrastructure

import com.byteslounge.slickrepo.repository.Repository
import com.dblazejewski.groups.{ Group, GroupRepository }
import slick.backend.DatabaseConfig
import slick.driver.JdbcProfile
import slick.dbio.DBIO

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
  val groupDal: Repository[Group, Int]
}

trait PersistenceModuleImpl extends PersistenceModule with DbModule {
  this: Configuration =>

  private val dbConfig: DatabaseConfig[JdbcProfile] = DatabaseConfig.forConfig("h2db")

  override implicit val profile: JdbcProfile = dbConfig.driver
  override implicit val db: JdbcProfile#Backend#Database = dbConfig.db

  override val groupDal = new GroupRepository(profile)

}
