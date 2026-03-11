package org.one2n.service

import io.micronaut.http.HttpStatus
import io.micronaut.http.exceptions.HttpStatusException
import jakarta.inject.Singleton
import org.one2n.dto.StudentRequest
import org.one2n.dto.StudentResponse
import org.one2n.entity.Student
import org.one2n.repository.StudentRepository
import org.slf4j.LoggerFactory
import java.util.UUID

@Singleton
class StudentService(
    private val studentRepository: StudentRepository,
) {
    private val logger = LoggerFactory.getLogger(StudentService::class.java)

    fun getAllStudents(): List<StudentResponse> {
        val students = studentRepository.findAll().toList()
        logger.debug("Fetching all students, count={}", students.size)

        return students.map { it.toResponse() }
    }

    fun createStudent(request: StudentRequest): StudentResponse {
        logger.debug("Creating new student")

        val student =
            Student(
                id = UUID.randomUUID(),
                name = request.name,
                age = request.age,
                email = request.email,
            )

        val savedStudent = studentRepository.save(student)

        logger.info("Student created with id={}", savedStudent.id)
        return savedStudent.toResponse()
    }

    fun getStudentById(id: UUID): StudentResponse {
        logger.debug("Looking up student with id={}", id)
        return findStudentOrThrow(id).toResponse()
    }

    private fun findStudentOrThrow(id: UUID): Student {
        val student = studentRepository.findById(id)

        if (student.isEmpty) {
            logger.warn("Student not found for id={}", id)
            throw HttpStatusException(HttpStatus.NOT_FOUND, "Student not found")
        }

        return student.get()
    }

    fun updateStudent(
        id: UUID,
        request: StudentRequest,
    ): StudentResponse {
        logger.debug("Updating student with id={}", id)

        val existingStudent = findStudentOrThrow(id)

        val updatedStudent =
            existingStudent.copy(
                name = request.name,
                age = request.age,
                email = request.email,
            )

        val savedStudent = studentRepository.update(updatedStudent)

        logger.info("Student updated with id={}", id)
        return savedStudent.toResponse()
    }

    fun deleteStudent(id: UUID): StudentResponse {
        logger.debug("Deleting student with id={}", id)

        val student = findStudentOrThrow(id)

        studentRepository.deleteById(id)

        logger.info("Student deleted with id={}", id)
        return student.toResponse()
    }

    private fun Student.toResponse(): StudentResponse {
        return StudentResponse(
            id = this.id,
            name = this.name,
            age = this.age,
            email = this.email,
        )
    }
}
