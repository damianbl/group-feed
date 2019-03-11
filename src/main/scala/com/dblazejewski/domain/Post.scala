package com.dblazejewski.domain

import java.time.LocalDateTime

case class Post(id: Option[Int],
                authorId: Int,
                groupId: Int,
                createdAt: LocalDateTime,
                content: String) extends IdEntity
