package com.dblazejewski.domain

case class User(id: Option[Long], name: String) extends IdEntity

object User {
  def create(name: String) = User(None, name)
}
