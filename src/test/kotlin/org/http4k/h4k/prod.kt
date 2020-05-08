package org.http4k.h4k

import org.http4k.cloudnative.env.Environment
import org.http4k.cloudnative.env.EnvironmentKey
import org.http4k.core.HttpHandler
import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.lens.int
import org.http4k.server.SunHttp
import org.http4k.server.asServer

data class InternalServiceId(val name: String) {
    override fun toString() = name
}

data class ExternalServiceId(val name: String) {
    override fun toString() = name
}

object App {
    val ID = InternalServiceId("app")
    private val port = EnvironmentKey.int().required(Proxy.ID.toString() + "_PORT")

    operator fun invoke(reverserHttp: HttpHandler): HttpHandler {
        val reverser = Reverser.Client(reverserHttp)
        return { _: Request -> Response(Status.OK).body(reverser.reverse("hello world")) }
    }

    fun Server(environment: Environment, registry: Registry<InternalServiceId>, egress: Discovery<ExternalServiceId>) =
        App(egress.lookup(ExternalServiceId("reverser")))
            .asServer(RegisteringServerConfig(ID, registry, SunHttp(port(environment))))
}

object Proxy {
    val ID = InternalServiceId("proxy")
    private val port = EnvironmentKey.int().required(ID.toString() + "_PORT")

    operator fun invoke(appHttp: HttpHandler) = { req: Request -> appHttp(req) }

    fun Server(environment: Environment, registry: Registry<InternalServiceId>) =
        Proxy(registry.lookup(App.ID)).asServer(RegisteringServerConfig(ID, registry, SunHttp(port(environment))))
}

object Reverser {
    val ID = ExternalServiceId("reverser")

    class Client(private val http: HttpHandler) {
        fun reverse(input: String) = http(Request(Method.GET, "/").body(input)).bodyString()
    }
}

