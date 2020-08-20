package org.http4k.h4k.example.main.internal

import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.h4k.example.lib.Discovery
import org.http4k.h4k.example.main.InternalServiceId

/**
 * Proxies requests to the App service
 */
object Proxy {
    val ID = InternalServiceId("proxy")

    operator fun invoke(discovery: Discovery<InternalServiceId>): (Request) -> Response {
        val app = discovery.lookup(Main.ID)
        return { req: Request -> app(req) }
    }

    fun main() = ProdAppServer() { Proxy(InternalDiscovery()) }.start()
}

