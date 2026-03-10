package org.one2n.controller

import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import org.one2n.dto.StudentResponse
import org.one2n.service.StudentService

@Controller("/api/v1/students")
class StudentController(
    private val studentService: StudentService
) {

    @Get
    fun getAllStudents(): List<StudentResponse> {
        return studentService.getAllStudents()
    }
}
