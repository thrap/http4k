package org.http4k.h4k.example.main

import org.http4k.core.HttpHandler
import org.http4k.core.Method.GET
import org.http4k.core.Request

/**
 * Domain client for the 3rd party Reverser service
 */
object Reverser {
    val ID = ExternalServiceId("reverser")

    class Client(private val http: HttpHandler) {
        fun reverse(input: String) = http(Request(GET, "/").body(input)).bodyString()
    }
}
