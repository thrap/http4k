package org.http4k.h4k.example.main.external.reverser

import org.http4k.core.HttpHandler
import org.http4k.core.Method.POST
import org.http4k.core.Request
import org.http4k.h4k.example.main.ExternalServiceId

/**
 * Domain client for the 3rd party Reverser service
 */
interface Reverser : (String) -> String {
    companion object
}

val Reverser.Companion.ID get() = ExternalServiceId("reverser")

fun Reverser.Companion.Http(http: HttpHandler) = object : Reverser {
    override operator fun invoke(input: String) = http(Request(POST, "/").body(input)).bodyString()
}
