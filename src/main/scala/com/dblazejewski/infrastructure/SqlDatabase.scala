package com.dblazejewski.infrastructure

import slick.jdbc.JdbcProfile

case class SqlDatabase(db: JdbcProfile#Backend#Database, driver: JdbcProfile)
