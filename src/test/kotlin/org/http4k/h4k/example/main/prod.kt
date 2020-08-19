package org.http4k.h4k.example.main

import org.http4k.core.HttpHandler
import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.h4k.example.lib.Discovery

data class InternalServiceId(val name: String)
data class ExternalServiceId(val name: String)

/**
 * Proxies requests to the App service
 */
object Proxy {
    val ID = InternalServiceId("proxy")

    operator fun invoke(discovery: Discovery<InternalServiceId>): (Request) -> Response {
        val app = discovery.lookup(App.ID)
        return { req: Request -> app(req) }
    }
}

/**
 * This is a particular application which uses the 3rd party Reverser service
 */
object App {
    val ID = InternalServiceId("app")

    operator fun invoke(discovery: Discovery<ExternalServiceId>): HttpHandler {
        val reverser = Reverser.Client(discovery.lookup(Reverser.ID))
        return { _: Request -> Response(Status.OK).body(reverser.reverse("hello world")) }
    }
}

/**
 * Domain client for the 3rd party Reverser service
 */
object Reverser {
    val ID = ExternalServiceId("reverser")

    class Client(private val http: HttpHandler) {
        fun reverse(input: String) = http(Request(Method.GET, "/").body(input)).bodyString()
    }
}
