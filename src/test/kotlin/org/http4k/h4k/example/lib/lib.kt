package org.http4k.h4k.example.lib

import org.http4k.cloudnative.env.Port
import org.http4k.core.HttpHandler
import org.http4k.server.Http4kServer
import org.http4k.server.SunHttp
import org.http4k.server.asServer

interface Discovery<ServiceId> {
    /**
     * Lookup a Service HttpHandler by it's identifier
     */
    fun lookup(id: ServiceId): HttpHandler
}

class H4KCluster<ServiceId>(private val log: (String) -> Unit = ::println) : Discovery<ServiceId> {
    private val services = mutableMapOf<ServiceId, HttpHandler>()
    private val servers = mutableListOf<Pair<ServiceId, Http4kServer>>()

    override fun lookup(id: ServiceId) = services[id]
        ?: throw IllegalStateException("$id is not registered in this cluster")

    /**
     * Install a Service HttpHandler into the cluster
     */
    fun install(id: ServiceId, appFn: (Discovery<ServiceId>) -> HttpHandler) = apply {
        val app = appFn(this)
        services[id] = app
    }

    /**
     * Bind a Service HttpHandler to an HTTP port
     */
    fun expose(id: ServiceId, port: Port) = apply {
        servers += id to lookup(id).asServer(SunHttp(port.value))
    }

    fun start() = apply {
        servers.forEach {
            it.second.start()
            log("Bound ${it.first} to ${it.second.port()}")
        }
    }

    fun stop() = apply {
        servers.forEach {
            it.second.stop()
            log("Unbound ${it.first}")
        }
    }
}
