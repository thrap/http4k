package org.http4k.h4k.example.main

import org.http4k.core.HttpHandler
import org.http4k.core.Method.POST
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status.Companion.OK
import org.http4k.h4k.example.lib.Discovery

/**
    Call flow is:
      Proxy -> App -> Doubler (ext)
                   -> Reverser (ext)
 */

sealed class ServiceId(val name: String)
class InternalServiceId(name: String) : ServiceId(name)
class ExternalServiceId(name: String) : ServiceId(name)

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
        val reverser = Reverser.Http(discovery.lookup(Reverser.ID))
        val doubler = Doubler.Http(discovery.lookup(Doubler.ID))
        return { _: Request -> Response(OK).body(reverser(doubler("hello world"))) }
    }
}

/**
 * Domain client for the 3rd party Reverser service
 */
object Reverser {
    val ID = ExternalServiceId("reverser")

    class Http(private val http: HttpHandler) {
        operator fun invoke(input: String) = http(Request(POST, "/").body(input)).bodyString()
    }
}

/**
 * Domain client for the 3rd party Doubler service
 */
object Doubler {
    val ID = ExternalServiceId("doubler")

    class Http(private val http: HttpHandler) {
        operator fun invoke(input: String) = http(Request(POST, "/").body(input)).bodyString()
    }
}
