package com.dblazejewski.domain

import java.time.LocalDateTime
import java.util.UUID

case class Post(id: UUID,
                authorId: UUID,
                groupId: UUID,
                createdAt: LocalDateTime,
                content: String) extends IdEntity
