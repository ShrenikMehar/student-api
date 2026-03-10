package org.one2n.controller

import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Delete
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.PathVariable
import io.micronaut.http.annotation.Post
import io.micronaut.http.annotation.Put
import org.one2n.dto.StudentRequest
import org.one2n.dto.StudentResponse
import org.one2n.service.StudentService
import org.slf4j.LoggerFactory
import java.util.UUID

@Controller("/api/v1/students")
class StudentController(
    private val studentService: StudentService
) {

    private val logger = LoggerFactory.getLogger(StudentController::class.java)

    @Get
    fun getAllStudents(): List<StudentResponse> {
        logger.debug("Received request to fetch all students")
        val students = studentService.getAllStudents()

        logger.debug("Returning {} students", students.size)
        return students
    }

    @Post
    fun createStudent(@Body request: StudentRequest): StudentResponse {
        logger.debug("Received request to create student")
        val student = studentService.createStudent(request)

        logger.info("Student created successfully with id={}", student.id)
        return student
    }

    @Get("/{id}")
    fun getStudentById(@PathVariable id: UUID): StudentResponse {
        logger.debug("Received request to fetch student with id={}", id)
        val student = studentService.getStudentById(id)

        logger.debug("Successfully fetched student with id={}", id)
        return student
    }

    @Put("/{id}")
    fun updateStudent(@PathVariable id: UUID, @Body request: StudentRequest): StudentResponse {
        logger.debug("Received request to update student with id={}", id)

        val updatedStudent = studentService.updateStudent(id, request)
        logger.info("Student updated successfully with id={}", id)

        return updatedStudent
    }

    @Delete("/{id}")
    fun deleteStudent(@PathVariable id: UUID): StudentResponse {
        logger.debug("Received request to delete student with id={}", id)
        val deletedStudent = studentService.deleteStudent(id)

        logger.info("Student deleted successfully with id={}", id)
        return deletedStudent
    }
}
