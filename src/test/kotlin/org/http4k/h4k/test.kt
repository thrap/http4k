package org.http4k.h4k

import org.http4k.cloudnative.env.Environment
import org.http4k.cloudnative.env.Environment.Companion.ENV
import org.http4k.core.HttpHandler
import org.http4k.core.Method.GET
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status.Companion.OK
import org.http4k.server.SunHttp
import org.http4k.server.asServer

fun Reverser.App(): HttpHandler = { req: Request -> Response(OK).body(req.bodyString().reversed()) }
fun Reverser.Server(registry: Registry<ExternalServiceId>) = App().asServer(RegisteringServerConfig(ID, registry) { SunHttp(8080) })

object K8SCluster {
    @JvmStatic
    fun main(args: Array<String>) = runClusterWith(ENV, K8SServiceRegistry(), EnvironmentConfiguredDiscovery(ENV))
}

object LocalCluster {
    @JvmStatic
    fun main(args: Array<String>) {
        val egress = LocalRegistry<ExternalServiceId>()
        runFakesWith(egress)
        runClusterWith(ENV, LocalRegistry(), egress)
    }
}

object H4KCluster {
    @JvmStatic
    fun main(args: Array<String>) {
        val egress = LocalRegistry<ExternalServiceId>()
        runFakesWith(egress)
        runClusterWith(ENV, H4KRegistry(), egress)
    }
}

fun runFakesWith(egress: Registry<ExternalServiceId>) {
    Reverser.Server(egress).start()
}

fun runClusterWith(environment: Environment, registry: Registry<InternalServiceId>, egress: Discovery<ExternalServiceId>) {
    App.Server(environment, registry, egress).start()
    Proxy.Server(environment, registry).start()

    val client = registry.lookup(Proxy.ID)
    println(client(Request(GET, "/")))
}
