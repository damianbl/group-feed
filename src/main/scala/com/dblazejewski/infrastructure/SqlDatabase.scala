package com.dblazejewski.infrastructure

import slick.driver.JdbcProfile

case class SqlDatabase(db: JdbcProfile#Backend#Database, driver: JdbcProfile) {
  def close() {
    db.close()
  }
}
