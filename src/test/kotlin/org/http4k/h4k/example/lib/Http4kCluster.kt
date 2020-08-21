package org.http4k.h4k.example.lib

import org.http4k.client.OkHttp
import org.http4k.cloudnative.env.Port
import org.http4k.core.HttpHandler
import org.http4k.core.Method.GET
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.h4k.example.main.InternalServiceId
import org.http4k.server.Http4kServer
import org.http4k.server.SunHttp
import org.http4k.server.asServer

/**
 * Run a cluster of HttpHandlers in a way that they can be wired together
 */
class H4KCluster<ServiceId>(private val log: (String) -> Unit = ::println) : Discovery<ServiceId>, HttpHandler {
    private val services = mutableMapOf<String, HttpHandler>()
    private val servers = mutableListOf<Pair<ServiceId, Http4kServer>>()

    override fun lookup(id: ServiceId) = services[id.toString()]
        ?: throw IllegalStateException("$id is not registered in this cluster")

    /**
     * Install a Service HttpHandler into the cluster
     */
    fun install(id: ServiceId, ingressPort: Port? = null, appFn: (Discovery<ServiceId>) -> HttpHandler) = apply {
        val app = appFn(this)
        services[id.toString()] = app
        ingressPort?.let { expose(id, it) }
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

        asServer(SunHttp(8000)).start()
    }

    fun stop() = apply {
        servers.forEach {
            it.second.stop()
            log("Unbound ${it.first}")
        }
    }

    override fun invoke(p1: Request) = services[p1.header("X-forwarded-host")]?.invoke(p1) ?: Response(Status.NOT_FOUND)
}

fun main() {
    H4KCluster<InternalServiceId>()
        .install(InternalServiceId("host1")) { { Response(Status.OK).body("hello") } }
        .install(InternalServiceId("host2")) { { Response(Status.I_M_A_TEAPOT).body("hello") } }
        .start()

    println(OkHttp()(Request(GET, "http://host1.localhost:8000/asd").header("X-forwarded-host", "host1")))
    println(OkHttp()(Request(GET, "http://host2.localhost:8000/asd").header("X-forwarded-host", "host2")))
}
