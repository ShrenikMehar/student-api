package org.one2n.controller

import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpStatus
import io.micronaut.http.client.HttpClient
import io.micronaut.http.client.annotation.Client
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.runtime.EmbeddedApplication
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import jakarta.inject.Inject
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

@MicronautTest(environments = ["test"])
class HealthControllerTest {
    @Inject
    lateinit var application: EmbeddedApplication<*>

    @Inject
    @field:Client("/")
    lateinit var client: HttpClient

    @Test
    fun testItWorks() {
        assertTrue(application.isRunning)
    }

    @Test
    fun `healthcheck returns degraded when database is unavailable`() {
        val exception =
            assertThrows<HttpClientResponseException> {
                client.toBlocking().exchange(HttpRequest.GET<Any>("/healthcheck"), Map::class.java)
            }

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, exception.status)
        assertEquals("degraded", exception.response.getBody(Map::class.java).get()["status"])
        assertEquals("unavailable", exception.response.getBody(Map::class.java).get()["database"])
    }
}
