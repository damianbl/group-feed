package com.dblazejewski.domain

import java.time.LocalDateTime

case class Post(
  id: Option[Long],
  authorId: Long,
  groupId: Long,
  createdAt: LocalDateTime,
  content: String) extends IdEntity
