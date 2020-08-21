package org.http4k.h4k.example.main.internal

import org.http4k.client.OkHttp
import org.http4k.cloudnative.env.Environment.Companion.ENV
import org.http4k.cloudnative.env.EnvironmentKey
import org.http4k.core.HttpHandler
import org.http4k.core.Uri
import org.http4k.core.then
import org.http4k.events.AutoJsonEvents
import org.http4k.filter.ClientFilters.SetBaseUriFrom
import org.http4k.format.Jackson
import org.http4k.h4k.example.lib.Discovery
import org.http4k.h4k.example.lib.ServiceId
import org.http4k.h4k.example.main.ClientStack
import org.http4k.h4k.example.main.ExternalServiceId
import org.http4k.h4k.example.main.InternalServiceId
import org.http4k.h4k.example.main.ServerInfra
import org.http4k.lens.uri
import org.http4k.server.SunHttp
import org.http4k.server.asServer

class RunningServerInfra(val serviceId: ServiceId) : ServerInfra {
    override val env = ENV
    override val events = AutoJsonEvents(Jackson, ::println)

    override val internalDiscovery = object : Discovery<InternalServiceId> {
        override fun lookup(id: InternalServiceId) = SetBaseUriFrom(Uri.of(id.name + ":" + 10000))
            .then(ClientStack(env, events))
            .then(OkHttp())
    }

    override val externalDiscovery = object : Discovery<ExternalServiceId> {
        override fun lookup(id: ExternalServiceId) = SetBaseUriFrom(id.uriKey()(env)).then(OkHttp())
    }
}

fun ExternalServiceId.uriKey() = EnvironmentKey.uri().required(name.toUpperCase() + "_URL")

fun ServerInfra.asAppServer(toApp: ServerInfra.() -> HttpHandler) = toApp().asServer(SunHttp(10000))
