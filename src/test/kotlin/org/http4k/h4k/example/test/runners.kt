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
import org.http4k.h4k.example.lib.Discovery
import org.http4k.h4k.example.lib.EnvironmentConfiguredDiscovery
import org.http4k.h4k.example.lib.H4KCluster
import org.http4k.h4k.example.lib.K8SServiceRegistry
import org.http4k.h4k.example.lib.LocalPortBoundRegistry
import org.http4k.h4k.example.lib.RegisteringServerConfig
import org.http4k.h4k.example.lib.Registry
import org.http4k.h4k.example.main.App
import org.http4k.h4k.example.main.ExternalServiceId
import org.http4k.h4k.example.main.InternalServiceId
import org.http4k.h4k.example.main.Proxy
import org.http4k.h4k.example.main.Reverser
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
        val egress = H4KCluster<ExternalServiceId>()
            .install(Reverser.ID) { Reverser.App() }
            .expose(Reverser.ID, Port(10000))
            .start()

        val cluster = H4KCluster<InternalServiceId>()
            .install(App.ID) { App(egress.lookup(Reverser.ID)) }
            .install(Proxy.ID) { discovery -> Proxy(discovery.lookup(App.ID)) }
            .expose(Proxy.ID, Port(8000))
            .start()

        println(cluster.lookup(Proxy.ID)(Request(GET, "")))

        val client = ClientFilters.SetBaseUriFrom(Uri.of("http://localhost:8000")).then(OkHttp())
        println(client(Request(GET, "")))
    }
}

fun runClusterWith(environment: Environment, registry: Registry<InternalServiceId>, egress: Discovery<ExternalServiceId>) {
    App.Server(environment, registry, egress).start()
    Proxy.Server(environment, registry).start()

    val client = registry.lookup(Proxy.ID)
    println(client(Request(GET, "/")))
}
