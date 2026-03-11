package org.one2n.entity

import io.micronaut.data.annotation.Id
import io.micronaut.data.annotation.MappedEntity
import java.util.UUID

@MappedEntity("students")
data class Student(
    @field:Id
    val id: UUID,
    val name: String,
    val age: Int,
    val email: String,
)
