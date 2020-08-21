package org.http4k.h4k.example.main.internal.backend

import org.http4k.cloudnative.env.Environment
import org.http4k.core.HttpHandler
import org.http4k.core.Method.POST
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.core.then
import org.http4k.events.Events
import org.http4k.h4k.example.lib.Discovery
import org.http4k.h4k.example.main.ExternalServiceId
import org.http4k.h4k.example.main.InternalServiceId
import org.http4k.h4k.example.main.ServerStack
import org.http4k.h4k.example.main.external.doubler.Doubler
import org.http4k.h4k.example.main.external.doubler.Http
import org.http4k.h4k.example.main.external.doubler.ID
import org.http4k.h4k.example.main.internal.RunningServerInfra
import org.http4k.h4k.example.main.internal.asAppServer

interface Backend : (String) -> String {
    companion object
}

fun main() {
    RunningServerInfra(Backend.ID)
        .asAppServer { Backend.App(env, events, externalDiscovery) }.start()
}

val Backend.Companion.ID get() = InternalServiceId("main")

fun Backend.Companion.App(env: Environment, events: Events, discovery: Discovery<ExternalServiceId>): HttpHandler {
    val backend = Backend.Domain(Doubler.Http(discovery.lookup(Doubler.ID)))

    return ServerStack(env, events).then { Response(Status.OK).body(backend(it.bodyString())) }
}

fun Backend.Companion.Domain(doubler: Doubler) = object : Backend {
    override fun invoke(p1: String): String = doubler("hello world")
}

fun Backend.Companion.Http(http: HttpHandler) = object : Backend {
    override fun invoke(p1: String) = http(Request(POST, "").body(p1)).bodyString()
}
