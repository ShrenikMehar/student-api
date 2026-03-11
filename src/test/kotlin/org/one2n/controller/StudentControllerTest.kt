package org.one2n.controller

import io.micronaut.http.HttpStatus
import io.micronaut.http.exceptions.HttpStatusException
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.one2n.dto.StudentResponse
import org.one2n.service.StudentService
import org.one2n.util.StudentTestData
import java.util.UUID

class StudentControllerTest {
    private val studentService = mockk<StudentService>()
    private val controller = StudentController(studentService)

    @Test
    fun `should return empty student list`() {
        every { studentService.getAllStudents() } returns emptyList()

        val result = controller.getAllStudents()

        assertEquals(0, result.size)
    }

    @Test
    fun `should create student`() {
        val request = StudentTestData.studentRequest()

        val response =
            StudentResponse(
                id = UUID.randomUUID(),
                name = request.name,
                age = request.age,
                email = request.email,
            )

        every { studentService.createStudent(request) } returns response

        val result = controller.createStudent(request)

        assertEquals("Alice", result.name)
        assertEquals(20, result.age)
        assertEquals("alice@test.com", result.email)
    }

    @Test
    fun `should return student by id`() {
        val id = UUID.randomUUID()

        val response =
            StudentResponse(
                id = id,
                name = "Alice",
                age = 20,
                email = "alice@test.com",
            )

        every { studentService.getStudentById(id) } returns response

        val result = controller.getStudentById(id)

        assertEquals(id, result.id)
        assertEquals("Alice", result.name)
    }

    @Test
    fun `should throw exception when student not found`() {
        val id = UUID.randomUUID()

        every { studentService.getStudentById(id) } throws
            HttpStatusException(HttpStatus.NOT_FOUND, "Student not found")

        assertThrows(HttpStatusException::class.java) {
            controller.getStudentById(id)
        }
    }

    @Test
    fun `should update student`() {
        val id = UUID.randomUUID()

        val request =
            StudentTestData.studentRequest(
                name = "Bob",
                age = 25,
            )

        val response =
            StudentResponse(
                id = id,
                name = "Bob",
                age = 25,
                email = request.email,
            )

        every { studentService.updateStudent(id, request) } returns response

        val result = controller.updateStudent(id, request)

        assertEquals(id, result.id)
        assertEquals("Bob", result.name)
        assertEquals(25, result.age)
    }

    @Test
    fun `should throw exception when updating non existing student`() {
        val id = UUID.randomUUID()

        val request =
            StudentTestData.studentRequest(
                name = "Bob",
                age = 25,
            )

        every { studentService.updateStudent(id, request) } throws
            HttpStatusException(HttpStatus.NOT_FOUND, "Student not found")

        assertThrows(HttpStatusException::class.java) {
            controller.updateStudent(id, request)
        }
    }

    @Test
    fun `should delete student`() {
        val id = UUID.randomUUID()

        val response =
            StudentResponse(
                id = id,
                name = "Alice",
                age = 20,
                email = "alice@test.com",
            )

        every { studentService.deleteStudent(id) } returns response

        val result = controller.deleteStudent(id)

        assertEquals(id, result.id)
    }

    @Test
    fun `should throw exception when deleting non existing student`() {
        val id = UUID.randomUUID()

        every { studentService.deleteStudent(id) } throws
            HttpStatusException(HttpStatus.NOT_FOUND, "Student not found")

        assertThrows(HttpStatusException::class.java) {
            controller.deleteStudent(id)
        }
    }
}
