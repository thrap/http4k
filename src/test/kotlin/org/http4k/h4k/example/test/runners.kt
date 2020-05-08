package org.http4k.h4k.example.test

import org.http4k.cloudnative.env.Environment
import org.http4k.cloudnative.env.Environment.Companion.ENV
import org.http4k.core.HttpHandler
import org.http4k.core.Method.GET
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status.Companion.OK
import org.http4k.h4k.Discovery
import org.http4k.h4k.EnvironmentConfiguredDiscovery
import org.http4k.h4k.InMemoryH4KCluster
import org.http4k.h4k.K8SServiceRegistry
import org.http4k.h4k.LocalPortBoundCluster
import org.http4k.h4k.RegisteringServerConfig
import org.http4k.h4k.Registry
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
        val egress = LocalPortBoundCluster<ExternalServiceId>()
        Reverser.Server(egress).start()
        runClusterWith(ENV, LocalPortBoundCluster(), egress)
    }
}

object RunH4KCluster {
    @JvmStatic
    fun main(args: Array<String>) {
        val egress = LocalPortBoundCluster<ExternalServiceId>()
        Reverser.Server(egress).start()

        runClusterWith(ENV, InMemoryH4KCluster(), egress)
    }
}

fun runClusterWith(environment: Environment, registry: Registry<InternalServiceId>, egress: Discovery<ExternalServiceId>) {
    App.Server(environment, registry, egress).start()
    Proxy.Server(environment, registry).start()

    val client = registry.lookup(Proxy.ID)
    println(client(Request(GET, "/")))
}
