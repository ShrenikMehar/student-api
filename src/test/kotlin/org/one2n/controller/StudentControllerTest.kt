package org.one2n.controller

import io.micronaut.http.HttpRequest
import io.micronaut.http.client.HttpClient
import io.micronaut.http.client.annotation.Client
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import jakarta.inject.Inject
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.one2n.dto.StudentResponse
import org.one2n.service.StudentService
import org.one2n.util.StudentTestData
import java.util.UUID

@MicronautTest
class StudentControllerTest {

    @Inject
    @field:Client("/")
    lateinit var client: HttpClient

    @Inject
    lateinit var studentService: StudentService

    @BeforeEach
    fun resetService() {
        val field = StudentService::class.java.getDeclaredField("students")
        field.isAccessible = true
        (field.get(studentService) as MutableList<*>).clear()
    }

    @Test
    fun `should return empty student list`() {
        val request = HttpRequest.GET<Any>("/api/v1/students")

        val response = client.toBlocking().retrieve(request)

        assertEquals("[]", response)
    }

    @Test
    fun `should create student`() {
        val requestBody = StudentTestData.studentRequest()
        val request = HttpRequest.POST("/api/v1/students", requestBody)

        val response = client.toBlocking()
            .retrieve(request, StudentResponse::class.java)

        assertEquals("Alice", response.name)
        assertEquals(20, response.age)
        assertEquals("alice@test.com", response.email)
    }

    @Test
    fun `should return student by id`() {
        val requestBody = StudentTestData.studentRequest()
        val request = HttpRequest.POST("/api/v1/students", requestBody)

        val createdStudent = client.toBlocking()
            .retrieve(request, StudentResponse::class.java)

        val getRequest = HttpRequest.GET<Any>("/api/v1/students/${createdStudent.id}")

        val fetchedStudent = client.toBlocking()
            .retrieve(getRequest, StudentResponse::class.java)

        assertEquals(createdStudent.id, fetchedStudent.id)
    }

    @Test
    fun `should return 404 when student not found`() {
        val id = UUID.randomUUID()

        val request = HttpRequest.GET<Any>("/api/v1/students/$id")

        assertThrows(HttpClientResponseException::class.java) {
            client.toBlocking().retrieve(request)
        }
    }

    @Test
    fun `should update student`() {
        val requestBody = StudentTestData.studentRequest()
        val createRequest = HttpRequest.POST("/api/v1/students", requestBody)

        val createdStudent = client.toBlocking()
            .retrieve(createRequest, StudentResponse::class.java)

        val updateRequest = StudentTestData.studentRequest(
            name = "Bob",
            age = 25
        )

        val putRequest = HttpRequest.PUT("/api/v1/students/${createdStudent.id}", updateRequest)

        val updatedStudent = client.toBlocking()
            .retrieve(putRequest, StudentResponse::class.java)

        assertEquals(createdStudent.id, updatedStudent.id)
        assertEquals("Bob", updatedStudent.name)
        assertEquals(25, updatedStudent.age)
    }

    @Test
    fun `should return 404 when updating non existing student`() {
        val id = UUID.randomUUID()

        val requestBody = StudentTestData.studentRequest(
            name = "Bob",
            age = 25
        )
        val request = HttpRequest.PUT("/api/v1/students/$id", requestBody)

        assertThrows(HttpClientResponseException::class.java) {
            client.toBlocking().retrieve(request)
        }
    }
}
