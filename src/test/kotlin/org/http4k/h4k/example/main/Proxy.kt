package org.http4k.h4k.example.main

import org.http4k.cloudnative.env.Environment
import org.http4k.cloudnative.env.EnvironmentKey
import org.http4k.core.HttpHandler
import org.http4k.core.Request
import org.http4k.h4k.example.lib.RegisteringServerConfig
import org.http4k.h4k.example.lib.Registry
import org.http4k.lens.int
import org.http4k.server.SunHttp
import org.http4k.server.asServer

/**
 * Proxies requests to the App service
 */
object Proxy {
    val ID = InternalServiceId("proxy")
    private val port = EnvironmentKey.int().required("PROXY_PORT")

    operator fun invoke(appHttp: HttpHandler) = { req: Request -> appHttp(req) }

    fun Server(
        environment: Environment,
        registry: Registry<InternalServiceId>
    ) =
        Proxy(registry.lookup(App.ID)).asServer(RegisteringServerConfig(ID, registry) { SunHttp(port(environment)) })
}
