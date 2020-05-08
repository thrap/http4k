package org.http4k.h4k

import org.http4k.cloudnative.env.Environment
import org.http4k.cloudnative.env.Environment.Companion.ENV
import org.http4k.cloudnative.env.Port
import org.http4k.core.HttpHandler
import org.http4k.core.Method.GET
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status.Companion.OK
import org.http4k.server.SunHttp
import org.http4k.server.asServer

val App.PORT get() = Port(9000)
val Proxy.PORT get() = Port(10000)
val Reverser.PORT get() = Port(8000)

fun Reverser.App(): HttpHandler = { req: Request -> Response(OK).body(req.bodyString().reversed()) }
fun Reverser.Server(registry: Registry<ExternalServiceId>) = App().asServer(RegisteringServerConfig(ID, registry, SunHttp(PORT.value)))

object K8SCluster {
    @JvmStatic
    fun main(args: Array<String>) = runClusterWith(K8SServiceRegistry(), EnvironmentConfiguredDiscovery(ENV), ENV)
}

object LocalCluster {
    @JvmStatic
    fun main(args: Array<String>) {
        val egress = LocalRegistry(mapOf(Reverser.ID to Reverser.PORT))
        runFakesWith(egress)
        runClusterWith(LocalRegistry(mapOf(App.ID to App.PORT, Proxy.ID to Proxy.PORT)), egress, ENV)
    }
}

object H4KCluster {
    @JvmStatic
    fun main(args: Array<String>) {
        val environment = Environment.from(
            "REVERSER_PORT" to "8000"
        )

        val egress = LocalRegistry(mapOf(Reverser.ID to Reverser.PORT))
        runFakesWith(egress)
        runClusterWith(H4KRegistry(), egress, environment)
    }
}

fun runFakesWith(egress: Registry<ExternalServiceId>) {
    Reverser.Server(egress).start()
}

fun runClusterWith(registry: Registry<InternalServiceId>, egress: Discovery<ExternalServiceId>, environment: Environment) {
    App.Server(environment, registry, egress).start()
    Proxy.Server(environment, registry).start()

    val client = registry.lookup(Proxy.ID)
    println(client(Request(GET, "/")))
}
