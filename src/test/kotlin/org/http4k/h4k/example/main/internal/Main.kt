package org.http4k.h4k.example.main.internal

import org.http4k.cloudnative.env.Environment
import org.http4k.core.HttpHandler
import org.http4k.core.Response
import org.http4k.core.Status.Companion.OK
import org.http4k.core.then
import org.http4k.events.Events
import org.http4k.h4k.example.lib.Discovery
import org.http4k.h4k.example.main.ExternalServiceId
import org.http4k.h4k.example.main.InternalServiceId
import org.http4k.h4k.example.main.ServerStack
import org.http4k.h4k.example.main.external.Doubler
import org.http4k.h4k.example.main.external.Reverser

/**
 * This is a particular application which uses the 3rd party Reverser service
 */
object Main {
    val ID = InternalServiceId("main")

    operator fun invoke(env: Environment, events: Events, discovery: Discovery<ExternalServiceId>): HttpHandler {
        val reverser = Reverser.Http(discovery.lookup(Reverser.ID))
        val doubler = Doubler.Http(discovery.lookup(Doubler.ID))
        return ServerStack(env, events).then { Response(OK).body(reverser(doubler("hello world"))) }
    }

    fun main() = RunningServerInfra(ID).asAppServer { Main(env, events, externalDiscovery) }.start()
}
