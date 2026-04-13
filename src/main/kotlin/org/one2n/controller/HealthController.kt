package org.one2n.controller

import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import org.slf4j.LoggerFactory
import java.util.Optional
import javax.sql.DataSource

@Controller
class HealthController(private val dataSource: Optional<DataSource>) {
    private val logger = LoggerFactory.getLogger(HealthController::class.java)

    @Get("/healthcheck")
    fun health(): HttpResponse<Map<String, String>> {
        val dbStatus = checkDatabase()
        val overall = if (dbStatus == "ok") "ok" else "degraded"
        val body = mapOf("status" to overall, "database" to dbStatus)
        return if (overall == "ok") HttpResponse.ok(body) else HttpResponse.serverError(body)
    }

    private fun checkDatabase(): String {
        if (!dataSource.isPresent) return "unavailable"
        return try {
            dataSource.get().connection.use { conn -> if (conn.isValid(1)) "ok" else "error" }
        } catch (e: java.sql.SQLException) {
            logger.warn("Database health check failed", e)
            "error"
        }
    }
}
