package org.http4k.h4k.example.main.internal

import org.http4k.cloudnative.env.Environment.Companion.ENV
import org.http4k.core.HttpHandler
import org.http4k.events.AutoJsonEvents
import org.http4k.events.EventFilters.AddTimestamp
import org.http4k.events.EventFilters.AddZipkinTraces
import org.http4k.events.then
import org.http4k.format.Jackson
import org.http4k.h4k.example.main.ServerInfra
import org.http4k.server.SunHttp
import org.http4k.server.asServer

class ProdServerInfra : ServerInfra {
    override val env = ENV
    override val events = AddTimestamp().then(AddZipkinTraces()).then(AutoJsonEvents(Jackson, ::println))
    override val internalDiscovery = ProdInternalDiscovery()
    override val externalDiscovery = ProdExternalDiscovery(env)
}

fun ProdAppServer(toApp: ServerInfra.() -> HttpHandler) = ProdServerInfra().toApp().asServer(SunHttp(10000))
