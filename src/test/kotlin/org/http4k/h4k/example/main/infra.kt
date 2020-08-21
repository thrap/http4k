package org.http4k.h4k.example.main

import org.http4k.cloudnative.env.Environment
import org.http4k.core.Method
import org.http4k.core.Uri
import org.http4k.core.then
import org.http4k.events.Event
import org.http4k.events.Events
import org.http4k.filter.ClientFilters
import org.http4k.filter.ResponseFilters
import org.http4k.filter.ServerFilters
import org.http4k.filter.ServerFilters.RequestTracing
import org.http4k.h4k.example.lib.Discovery
import org.http4k.h4k.example.main.internal.Settings.CREDEMTIALS

/**
Call flow is:
    Proxy (http://proxy:10000) -> Main (http://main:10000) -> Doubler (http://doubler)
                                                           -> Reverser (http://reverser)
 */

sealed class ServiceId(val name: String) {
    override fun toString() = name
}

class InternalServiceId(name: String) : ServiceId(name)
class ExternalServiceId(name: String) : ServiceId(name)

interface ServerInfra {
    val env: Environment
    val events: Events
    val internalDiscovery: Discovery<InternalServiceId>
    val externalDiscovery: Discovery<ExternalServiceId>
}

fun RecordRequest(events: Events, toEvent: (Uri, Method, Int, Long) -> Event) = ResponseFilters.ReportHttpTransaction {
    events(
        toEvent(
            it.request.uri,
            it.request.method,
            it.response.status.code,
            it.duration.toMillis()
        )
    )
}

fun ServerStack(env: Environment, events: Events) =
    RecordRequest(events, ::IncomingHttpRequest)
        .then(ServerFilters.BasicAuth("", CREDEMTIALS(env)))
        .then(RequestTracing())

data class IncomingHttpRequest(
    val uri: Uri,
    val method: Method,
    val status: Int,
    val duration: Long
) : Event

fun ClientStack(env: Environment, events: Events) = RecordRequest(events, ::OutgoingHttpRequest)
    .then(ClientFilters.BasicAuth(CREDEMTIALS(env)))
    .then(RequestTracing())

data class OutgoingHttpRequest(
    val uri: Uri,
    val method: Method,
    val status: Int,
    val duration: Long
) : Event
