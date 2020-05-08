package org.http4k.h4k.example.test

import org.http4k.client.OkHttp
import org.http4k.cloudnative.env.Environment
import org.http4k.cloudnative.env.Environment.Companion.ENV
import org.http4k.cloudnative.env.Port
import org.http4k.core.HttpHandler
import org.http4k.core.Method.GET
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status.Companion.OK
import org.http4k.core.Uri
import org.http4k.core.then
import org.http4k.filter.ClientFilters
import org.http4k.h4k.Discovery
import org.http4k.h4k.EnvironmentConfiguredDiscovery
import org.http4k.h4k.K8SServiceRegistry
import org.http4k.h4k.LocalPortBoundRegistry
import org.http4k.h4k.RegisteringServerConfig
import org.http4k.h4k.Registry
import org.http4k.h4k.example.main.App
import org.http4k.h4k.example.main.ExternalServiceId
import org.http4k.h4k.example.main.InternalServiceId
import org.http4k.h4k.example.main.Proxy
import org.http4k.h4k.example.main.Reverser
import org.http4k.server.Http4kServer
import org.http4k.server.SunHttp
import org.http4k.server.asServer

fun Reverser.App(): HttpHandler = { req: Request -> Response(OK).body(req.bodyString().reversed()) }
fun Reverser.Server(registry: Registry<ExternalServiceId>) = App().asServer(RegisteringServerConfig(ID, registry) { SunHttp(8080) })

object RunK8SCluster {
    @JvmStatic
    fun main(args: Array<String>) = runClusterWith(ENV, K8SServiceRegistry(), EnvironmentConfiguredDiscovery(ENV))
}

object RunLocalCluster {
    @JvmStatic
    fun main(args: Array<String>) {
        val egress = LocalPortBoundRegistry<ExternalServiceId>()
        Reverser.Server(egress).start()
        runClusterWith(ENV, LocalPortBoundRegistry(), egress)
    }
}

object RunH4KCluster {
    @JvmStatic
    fun main(args: Array<String>) {

        val egress = LocalCluster<ExternalServiceId>()
            .running(Reverser.ID) { Reverser.App() }
            .start()

        LocalCluster<InternalServiceId>()
            .running(App.ID) { App(egress.lookup(Reverser.ID)) }
            .running(Proxy.ID, Port(8000)) { Proxy(it.lookup(App.ID)) }
            .start()

        val client = ClientFilters.SetBaseUriFrom(Uri.of("http://localhost:8000")).then(OkHttp())
        println(client(Request(GET, "")))
    }
}

class LocalCluster<ServiceId> : Discovery<ServiceId> {
    private val services = mutableMapOf<ServiceId, HttpHandler>()
    private val servers = mutableListOf<Pair<ServiceId, Http4kServer>>()

    override fun lookup(id: ServiceId) = services[id]
        ?: throw IllegalStateException("$id is not registered in this cluster")

    fun running(id: ServiceId, port: Port? = null, appFn: (Discovery<ServiceId>) -> HttpHandler) = apply {
        val app = appFn(this)
        services[id] = app
        port?.also { servers += (id to app.asServer(SunHttp(it.value))) }
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

fun runClusterWith(environment: Environment, registry: Registry<InternalServiceId>, egress: Discovery<ExternalServiceId>) {
    App.Server(environment, registry, egress).start()
    Proxy.Server(environment, registry).start()

    val client = registry.lookup(Proxy.ID)
    println(client(Request(GET, "/")))
}
