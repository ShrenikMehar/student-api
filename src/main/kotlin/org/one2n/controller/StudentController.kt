package org.one2n.controller

import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.PathVariable
import io.micronaut.http.annotation.Post
import org.one2n.dto.StudentRequest
import org.one2n.dto.StudentResponse
import org.one2n.service.StudentService
import java.util.UUID

@Controller("/api/v1/students")
class StudentController(
    private val studentService: StudentService
) {

    @Get
    fun getAllStudents(): List<StudentResponse> {
        return studentService.getAllStudents()
    }

    @Post
    fun createStudent(@Body request: StudentRequest): StudentResponse {
        return studentService.createStudent(request)
    }

    @Get("/{id}")
    fun getStudentById(@PathVariable id: UUID): StudentResponse {
        return studentService.getStudentById(id)
    }
}
