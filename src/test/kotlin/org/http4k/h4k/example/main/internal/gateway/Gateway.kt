package org.http4k.h4k.example.main.internal.gateway

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
import org.http4k.h4k.example.main.external.reverser.Http
import org.http4k.h4k.example.main.external.reverser.ID
import org.http4k.h4k.example.main.external.reverser.Reverser
import org.http4k.h4k.example.main.internal.RunningServerInfra
import org.http4k.h4k.example.main.internal.asAppServer
import org.http4k.h4k.example.main.internal.backend.Backend
import org.http4k.h4k.example.main.internal.backend.Http
import org.http4k.h4k.example.main.internal.backend.ID

interface Gateway : (String) -> String {
    companion object
}

fun main() {
    RunningServerInfra(Gateway.ID)
        .asAppServer { Gateway.App(env, events, internalDiscovery, externalDiscovery) }
        .start()
}

val Gateway.Companion.ID get() = InternalServiceId("main")

fun Gateway.Companion.App(
    env: Environment, events: Events,
    internal: Discovery<InternalServiceId>,
    external: Discovery<ExternalServiceId>
): HttpHandler {
    val gateway = Gateway.Domain(
        Reverser.Http(external.lookup(Reverser.ID)),
        Backend.Http(internal.lookup(Backend.ID))
    )
    return ServerStack(env, events).then { Response(OK).body(gateway(it.bodyString())) }
}

fun Gateway.Companion.Domain(reverser: Reverser, backend: Backend) = object : Gateway {
    override fun invoke(p1: String): String = backend(reverser(p1))
}
