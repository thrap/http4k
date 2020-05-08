package org.http4k.h4k.example.lib


import org.http4k.client.OkHttp
import org.http4k.cloudnative.env.Environment
import org.http4k.cloudnative.env.EnvironmentKey
import org.http4k.cloudnative.env.Port
import org.http4k.core.HttpHandler
import org.http4k.core.Uri
import org.http4k.core.then
import org.http4k.filter.ClientFilters
import org.http4k.lens.uri
import org.http4k.server.Http4kServer
import org.http4k.server.ServerConfig

/**
 * Use this when running an app in a deployed K8S cluster to register and lookup other K8S services
 */
class K8SServiceRegistry<ServiceId>(private val port: (ServiceId) -> Port = { Port(8080) }) : Registry<ServiceId> {

    private var server: Http4kServer? = null

    override fun register(id: ServiceId, http: HttpHandler): ServerControl = {
        if (server != null) throw IllegalStateException("There is already a server started in this pod!")
        server = it().start()
    }

    override fun unregister(id: ServiceId) {
        server?.stop()
    }

    override fun lookup(id: ServiceId) = ClientFilters.SetHostFrom(Uri.of("http://${id}:${port(id).value}")).then(OkHttp())
}

/**
 * Use this in locally running cluster mode (all port bound services)
 */
class LocalPortBoundRegistry<ServiceId> : Registry<ServiceId> {

    private val services = mutableMapOf<ServiceId, Http4kServer>()

    override fun register(id: ServiceId, http: HttpHandler): ServerControl = {
        services[id] = it().start()
    }

    override fun unregister(id: ServiceId) {
        services[id]?.stop()
    }

    override fun lookup(id: ServiceId) = ClientFilters.SetHostFrom(Uri.of("http://localhost:${services[id]}")).then(OkHttp())
}

/**
 * Use this when running in deployed K8S cluster to lookup External services by URL in Environmental config
 */
class EnvironmentConfiguredDiscovery<ExternalServiceId>(private val environment: Environment = Environment.ENV) : Discovery<ExternalServiceId> {
    override fun lookup(id: ExternalServiceId): HttpHandler {
        val lens = EnvironmentKey.uri().required(id.toString().toUpperCase() + "_URL")
        return ClientFilters.SetHostFrom(lens(environment)).then(OkHttp())
    }
}

class RegisteringServerConfig<ServiceId>(
    private val id: ServiceId,
    private val registry: Registry<ServiceId>,
    private val serverConfig: () -> ServerConfig) : ServerConfig {

    override fun toServer(httpHandler: HttpHandler) = object : Http4kServer {
        private val server by lazy { serverConfig().toServer(httpHandler) }

        override fun start() = apply {
            registry.register(id, httpHandler).invoke { server }
        }

        override fun stop() = apply {
            registry.unregister(id)
        }

        override fun port() = server.port()
    }
}
