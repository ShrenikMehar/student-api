package org.one2n.controller

import io.micronaut.http.HttpRequest
import io.micronaut.http.client.HttpClient
import io.micronaut.http.client.annotation.Client
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import jakarta.inject.Inject
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

@MicronautTest(environments = ["test"])
class HealthControllerTest {
    @Inject
    @field:Client("/")
    lateinit var client: HttpClient

    @Test
    fun `healthcheck endpoint returns ok`() {
        val request = HttpRequest.GET<Any>("/healthcheck")
        val response = client.toBlocking().retrieve(request)

        assertEquals("""{"status":"ok"}""", response)
    }
}
