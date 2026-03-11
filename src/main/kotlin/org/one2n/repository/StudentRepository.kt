package org.one2n.repository

import io.micronaut.data.jdbc.annotation.JdbcRepository
import io.micronaut.data.model.query.builder.sql.Dialect
import io.micronaut.data.repository.CrudRepository
import org.one2n.entity.Student
import java.util.UUID

@JdbcRepository(dialect = Dialect.POSTGRES)
interface StudentRepository : CrudRepository<Student, UUID>
