package org.one2n.service

import jakarta.inject.Singleton
import org.one2n.dto.StudentRequest
import org.one2n.dto.StudentResponse
import java.util.UUID

@Singleton
class StudentService {

    private val students = mutableListOf<StudentResponse>()

    fun getAllStudents(): List<StudentResponse> {
        return students
    }

    fun createStudent(request: StudentRequest): StudentResponse {

        val student = StudentResponse(
            id = UUID.randomUUID(),
            name = request.name,
            age = request.age,
            email = request.email
        )

        students.add(student)

        return student
    }
}
