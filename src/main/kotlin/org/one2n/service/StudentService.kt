package org.one2n.service

import io.micronaut.http.HttpStatus
import io.micronaut.http.exceptions.HttpStatusException
import jakarta.inject.Singleton
import org.one2n.dto.StudentRequest
import org.one2n.dto.StudentResponse
import org.slf4j.LoggerFactory
import java.util.UUID

@Singleton
class StudentService {

    private val students = mutableListOf<StudentResponse>()
    private val logger = LoggerFactory.getLogger(StudentService::class.java)

    fun getAllStudents(): List<StudentResponse> {
        logger.debug("Fetching all students, count={}", students.size)
        return students
    }

    fun createStudent(request: StudentRequest): StudentResponse {
        logger.debug("Creating new student")
        val student = StudentResponse(
            id = UUID.randomUUID(),
            name = request.name,
            age = request.age,
            email = request.email
        )

        students.add(student)

        logger.info("Student created with id={}", student.id)
        return student
    }

    fun getStudentById(id: UUID): StudentResponse {
        logger.debug("Looking up student with id={}", id)
        return findStudentOrThrow(id)
    }

    private fun findStudentOrThrow(id: UUID): StudentResponse {
        val student = students.find { it.id == id }

        if (student == null) {
            logger.warn("Student not found for id={}", id)
            throw HttpStatusException(HttpStatus.NOT_FOUND, "Student not found")
        }

        return student
    }

    fun updateStudent(id: UUID, request: StudentRequest): StudentResponse {
        logger.debug("Updating student with id={}", id)
        val existingStudent = findStudentOrThrow(id)

        val updatedStudent = StudentResponse(
            id = id,
            name = request.name,
            age = request.age,
            email = request.email
        )

        val index = students.indexOf(existingStudent)
        students[index] = updatedStudent

        logger.info("Student updated with id={}", id)
        return updatedStudent
    }

    fun deleteStudent(id: UUID): StudentResponse {
        logger.debug("Deleting student with id={}", id)
        val student = findStudentOrThrow(id)

        students.remove(student)

        logger.info("Student deleted with id={}", id)
        return student
    }
}
