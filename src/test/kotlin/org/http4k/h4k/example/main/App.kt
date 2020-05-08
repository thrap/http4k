package org.http4k.h4k.example.main

import org.http4k.cloudnative.env.Environment
import org.http4k.cloudnative.env.EnvironmentKey
import org.http4k.core.HttpHandler
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.h4k.Discovery
import org.http4k.h4k.RegisteringServerConfig
import org.http4k.h4k.Registry
import org.http4k.lens.int
import org.http4k.server.SunHttp
import org.http4k.server.asServer

/**
 * This is a particular application which uses the 3rd party Reverser service
 */
object App {
    val ID = InternalServiceId("app")
    private val port = EnvironmentKey.int().required("APP_PORT")

    operator fun invoke(reverserHttp: HttpHandler): HttpHandler {
        val reverser = Reverser.Client(reverserHttp)
        return { _: Request -> Response(Status.OK).body(reverser.reverse("hello world")) }
    }

    fun Server(
        environment: Environment,
        registry: Registry<InternalServiceId>,
        egress: Discovery<ExternalServiceId>
    ) =
        App(egress.lookup(Reverser.ID)).asServer(RegisteringServerConfig(ID, registry) { SunHttp(port(environment)) })
}
