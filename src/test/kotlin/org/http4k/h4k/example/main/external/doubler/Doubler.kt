package org.http4k.h4k.example.main.external.doubler

import org.http4k.core.HttpHandler
import org.http4k.core.Method.POST
import org.http4k.core.Request
import org.http4k.h4k.example.main.ExternalServiceId

/**
 * Domain client for the 3rd party Doubler service
 */
interface Doubler : (String) -> String {
    companion object
}

val Doubler.Companion.ID get() = ExternalServiceId("doubler")

fun Doubler.Companion.Http(http: HttpHandler) = object : Doubler {
    override operator fun invoke(input: String) = http(Request(POST, "/").body(input)).bodyString()
}
