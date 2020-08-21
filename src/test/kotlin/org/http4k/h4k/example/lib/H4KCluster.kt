package org.http4k.h4k.example.lib

import org.http4k.client.OkHttp
import org.http4k.core.Filter
import org.http4k.core.HttpHandler
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status.Companion.NOT_FOUND
import org.http4k.core.then
import org.http4k.server.Http4kServer
import org.http4k.server.ServerConfig
import org.http4k.server.SunHttp
import org.http4k.server.asServer

interface ServiceId {
    val name: String
}

class H4KCluster<Id : ServiceId>(serverConfig: ServerConfig = SunHttp(0)) : HttpHandler, Discovery<Id>,
    Http4kServer {
    private val server = asServer(serverConfig)
    private val services = mutableMapOf<String, HttpHandler>()
    private val exposed = mutableSetOf<String>()

    fun deploy(id: Id, appFn: (Discovery<Id>) -> HttpHandler) = apply {
        services[id.name] = appFn(this)
    }

    fun expose(id: Id) = apply {
        exposed += id.name
    }

    override fun invoke(p1: Request) =
        with(p1.header("x-forwarded-host") ?: p1.header("host") ?: p1.uri.host) {
            services[this]?.takeIf { exposed.contains(this) }?.invoke(p1) ?: Response(NOT_FOUND)
        }

    override fun lookup(id: Id) = services[id.name]?.let { clientFor(id, it) }
        ?: throw IllegalArgumentException("No service ${id.name} installed in this cluster")

    fun clientFor(id: Id, http: HttpHandler = OkHttp()) = SetXForwardedHostFor(id).then(http)

    override fun start() = apply { server.start() }

    override fun stop() = apply { server.stop() }

    override fun port() = server.port()
}

private fun SetXForwardedHostFor(id: ServiceId) = Filter { next ->
    {
        next(it.header("x-forwarded-host", id.name))
    }
}
