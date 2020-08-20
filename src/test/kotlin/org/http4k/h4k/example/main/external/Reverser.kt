package org.http4k.h4k.example.main.external

import org.http4k.core.HttpHandler
import org.http4k.core.Method.POST
import org.http4k.core.Request
import org.http4k.h4k.example.main.ExternalServiceId

/**
 * Domain client for the 3rd party Reverser service
 */
object Reverser {
    val ID = ExternalServiceId("reverser")

    class Http(private val http: HttpHandler) {
        operator fun invoke(input: String) = http(Request(POST, "/").body(input)).bodyString()
    }
}
