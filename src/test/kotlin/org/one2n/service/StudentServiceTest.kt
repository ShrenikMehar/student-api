package org.one2n.service

import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class StudentServiceTest {

    private val studentService = StudentService()

    @Test
    fun `should return empty list when no students exist`() {
        val students = studentService.getAllStudents()

        assertTrue(students.isEmpty())
    }
}
