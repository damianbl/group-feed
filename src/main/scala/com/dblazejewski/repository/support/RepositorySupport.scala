package com.dblazejewski.repository.support

import com.dblazejewski.infrastructure.SqlDatabase
import slick.dbio.{DBIOAction, NoStream}

trait RepositorySupport extends UuidSupport {
  protected val database: SqlDatabase

  protected def runInDb[R](action: DBIOAction[R, NoStream, Nothing]) = database.db.run(action)

}
