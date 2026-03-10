package org.one2n.controller

import io.micronaut.http.HttpRequest
import io.micronaut.http.client.HttpClient
import io.micronaut.http.client.annotation.Client
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import jakarta.inject.Inject
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.one2n.dto.StudentRequest
import org.one2n.dto.StudentResponse

@MicronautTest
class StudentControllerTest {

    @Inject
    @field:Client("/")
    lateinit var client: HttpClient

    @Test
    fun `should return empty student list`() {
        val request = HttpRequest.GET<Any>("/api/v1/students")

        val response = client.toBlocking().retrieve(request)

        assertEquals("[]", response)
    }

    @Test
    fun `should create student`() {

        val requestBody = StudentRequest(
            name = "Alice",
            age = 20,
            email = "alice@test.com"
        )

        val request = HttpRequest.POST("/api/v1/students", requestBody)

        val response = client.toBlocking()
            .retrieve(request, StudentResponse::class.java)

        assertEquals("Alice", response.name)
        assertEquals(20, response.age)
        assertEquals("alice@test.com", response.email)
    }
}
