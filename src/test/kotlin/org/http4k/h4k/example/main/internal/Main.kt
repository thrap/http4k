package org.http4k.h4k.example.main.internal

import org.http4k.core.HttpHandler
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.h4k.example.lib.Discovery
import org.http4k.h4k.example.main.ExternalServiceId
import org.http4k.h4k.example.main.InternalServiceId
import org.http4k.h4k.example.main.external.Doubler
import org.http4k.h4k.example.main.external.Reverser

/**
 * This is a particular application which uses the 3rd party Reverser service
 */
object Main {
    val ID = InternalServiceId("main")

    operator fun invoke(discovery: Discovery<ExternalServiceId>): HttpHandler {
        val reverser = Reverser.Http(discovery.lookup(Reverser.ID))
        val doubler = Doubler.Http(discovery.lookup(Doubler.ID))
        return { _: Request -> Response(Status.OK).body(reverser(doubler("hello world"))) }
    }

    fun main() = ProdAppServer { Main(ExternalDiscovery()) }.start()
}
