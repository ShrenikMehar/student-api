package org.one2n.service

import io.micronaut.http.exceptions.HttpStatusException
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertNotNull
import org.one2n.entity.Student
import org.one2n.repository.StudentRepository
import org.one2n.util.StudentTestData
import java.util.Optional
import java.util.UUID

class StudentServiceTest {
    private val studentRepository = mockk<StudentRepository>()
    private val studentService = StudentService(studentRepository)

    @Test
    fun `should return empty list when no students exist`() {
        every { studentRepository.findAll() } returns emptyList()

        val students = studentService.getAllStudents()

        assertTrue(students.isEmpty())
    }

    @Test
    fun `should create student`() {
        val request = StudentTestData.studentRequest()

        val student =
            Student(
                id = UUID.randomUUID(),
                name = request.name,
                age = request.age,
                email = request.email,
            )

        every { studentRepository.save(any()) } returns student

        val result = studentService.createStudent(request)

        assertNotNull(result.id)
        assertEquals("Alice", result.name)
        assertEquals(20, result.age)
        assertEquals("alice@test.com", result.email)
    }

    @Test
    fun `should return student by id`() {
        val id = UUID.randomUUID()

        val student =
            Student(
                id = id,
                name = "Alice",
                age = 20,
                email = "alice@test.com",
            )

        every { studentRepository.findById(id) } returns Optional.of(student)

        val fetchedStudent = studentService.getStudentById(id)

        assertEquals(id, fetchedStudent.id)
        assertEquals("Alice", fetchedStudent.name)
    }

    @Test
    fun `should throw exception when student not found`() {
        val id = UUID.randomUUID()

        every { studentRepository.findById(id) } returns Optional.empty()

        assertThrows(HttpStatusException::class.java) {
            studentService.getStudentById(id)
        }
    }

    @Test
    fun `should update student`() {
        val id = UUID.randomUUID()

        val existing = Student(id, "Alice", 20, "alice@test.com")

        val updated = Student(id, "Bob", 25, "alice@test.com")

        every { studentRepository.findById(id) } returns Optional.of(existing)
        every { studentRepository.update(any()) } returns updated

        val request = StudentTestData.studentRequest(name = "Bob", age = 25)

        val result = studentService.updateStudent(id, request)

        assertEquals("Bob", result.name)
        assertEquals(25, result.age)
    }

    @Test
    fun `should throw exception when updating non existing student`() {
        val id = UUID.randomUUID()

        every { studentRepository.findById(id) } returns Optional.empty()

        val request = StudentTestData.studentRequest(name = "Bob", age = 25)

        assertThrows(HttpStatusException::class.java) {
            studentService.updateStudent(id, request)
        }
    }

    @Test
    fun `should delete student`() {
        val id = UUID.randomUUID()

        val student = Student(id, "Alice", 20, "alice@test.com")

        every { studentRepository.findById(id) } returns Optional.of(student)
        every { studentRepository.deleteById(id) } returns Unit

        val deleted = studentService.deleteStudent(id)

        verify { studentRepository.deleteById(id) }

        assertEquals(id, deleted.id)
    }

    @Test
    fun `should throw exception when deleting non existing student`() {
        val id = UUID.randomUUID()

        every { studentRepository.findById(id) } returns Optional.empty()

        assertThrows(HttpStatusException::class.java) {
            studentService.deleteStudent(id)
        }
    }
}
