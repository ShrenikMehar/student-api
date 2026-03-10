package org.one2n.service

import jakarta.inject.Singleton
import org.one2n.dto.StudentResponse

@Singleton
class StudentService {

    fun getAllStudents(): List<StudentResponse> {
        return emptyList()
    }

}
