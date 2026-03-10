package org.one2n.controller

import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get

@Controller
class HealthController {

    @Get("/healthcheck")
    fun health(): Map<String, String> {
        return mapOf("status" to "ok")
    }
}
