package org.http4k.h4k.example.lib

import org.http4k.cloudnative.env.Port
import org.http4k.core.HttpHandler
import org.http4k.server.Http4kServer
import org.http4k.server.SunHttp
import org.http4k.server.asServer

interface Discovery<ServiceId> {
    fun lookup(id: ServiceId): HttpHandler
}

typealias ServerControl = (() -> Http4kServer) -> Unit

interface Registry<ServiceId> : Discovery<ServiceId> {
    fun register(id: ServiceId, http: HttpHandler): ServerControl
    fun unregister(id: ServiceId)
}

class H4KCluster<ServiceId> : Discovery<ServiceId> {
    private val services = mutableMapOf<ServiceId, HttpHandler>()
    private val servers = mutableListOf<Pair<ServiceId, Http4kServer>>()

    override fun lookup(id: ServiceId) = services[id]
        ?: throw IllegalStateException("$id is not registered in this cluster")

    fun install(id: ServiceId, appFn: (Discovery<ServiceId>) -> HttpHandler) = apply {
        val app = appFn(this)
        services[id] = app
    }

    fun expose(id: ServiceId, port: Port) = apply {
        servers += id to lookup(id).asServer(SunHttp(port.value))
    }

    fun start() = apply {
        servers.forEach {
            it.second.start()
            println("Started ${it.first} on ${it.second.port()}")
        }
    }

    fun stop() = apply {
        servers.forEach {
            it.second.stop()
            println("Stopped ${it.first}")
        }
    }
}
