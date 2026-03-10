package org.one2n.service

import io.micronaut.http.exceptions.HttpStatusException
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.one2n.util.StudentTestData
import java.util.UUID

class StudentServiceTest {

    private val studentService = StudentService()

    @Test
    fun `should return empty list when no students exist`() {
        val students = studentService.getAllStudents()

        assertTrue(students.isEmpty())
    }

    @Test
    fun `should create student`() {
        val request = StudentTestData.studentRequest()

        val student = studentService.createStudent(request)

        assertNotNull(student.id)
        assertEquals("Alice", student.name)
        assertEquals(20, student.age)
        assertEquals("alice@test.com", student.email)
    }

    @Test
    fun `should return student by id`() {
        val request = StudentTestData.studentRequest()
        val createdStudent = studentService.createStudent(request)

        val fetchedStudent = studentService.getStudentById(createdStudent.id)

        assertEquals(createdStudent.id, fetchedStudent.id)
        assertEquals("Alice", fetchedStudent.name)
    }

    @Test
    fun `should throw exception when student not found`() {
        val id = UUID.randomUUID()

        assertThrows(HttpStatusException::class.java) {
            studentService.getStudentById(id)
        }
    }

    @Test
    fun `should update student`() {
        val request = StudentTestData.studentRequest()
        val createdStudent = studentService.createStudent(request)

        val updateRequest = StudentTestData.studentRequest(
            name = "Bob",
            age = 25
        )

        val updatedStudent = studentService.updateStudent(createdStudent.id, updateRequest)

        assertEquals(createdStudent.id, updatedStudent.id)
        assertEquals("Bob", updatedStudent.name)
        assertEquals(25, updatedStudent.age)
    }

    @Test
    fun `should throw exception when updating non existing student`() {
        val id = UUID.randomUUID()

        val request = StudentTestData.studentRequest(
            name = "Bob",
            age = 25
        )

        assertThrows(HttpStatusException::class.java) {
            studentService.updateStudent(id, request)
        }
    }
}
