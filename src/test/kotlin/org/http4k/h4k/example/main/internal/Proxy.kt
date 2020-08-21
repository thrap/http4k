package org.http4k.h4k.example.main.internal

import org.http4k.cloudnative.env.Environment
import org.http4k.core.then
import org.http4k.events.Events
import org.http4k.h4k.example.lib.Discovery
import org.http4k.h4k.example.main.InternalServiceId
import org.http4k.h4k.example.main.ServerStack

/**
 * Proxies requests to the App service
 */
object Proxy {
    val ID = InternalServiceId("proxy")

    operator fun invoke(env: Environment, events: Events, discovery: Discovery<InternalServiceId>) =
        ServerStack(env, events).then(discovery.lookup(Main.ID))

    fun main() = ProdAppServer { Proxy(env, events, internalDiscovery) }.start()
}

