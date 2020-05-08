package org.http4k.h4k


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

interface Discovery<ServiceId> {
    fun lookup(id: ServiceId): HttpHandler
}

interface Registry<ServiceId> : Discovery<ServiceId> {
    fun register(id: ServiceId, http: HttpHandler): Registry<ServiceId> = apply {}
    fun unregister(id: ServiceId): Registry<ServiceId> = apply {}
}

/**
 * Use this when running in deployed K8S cluster to register and lookup K8S services
 */
class K8SServiceRegistry<ServiceId> : Registry<ServiceId> {
    override fun lookup(id: ServiceId) = ClientFilters.SetHostFrom(Uri.of("http://${id}:8080")).then(OkHttp())
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

/**
 * Use this in locally running cluster mode (all port bound services)
 */
class LocalRegistry<ServiceId>(private val ports: Map<ServiceId, Port>) : Registry<ServiceId> {
    override fun lookup(id: ServiceId) = ClientFilters.SetHostFrom(Uri.of("http://localhost:${ports[id]?.value}")).then(OkHttp())
}

/**
 * Use this for an entirely in-memory cluster
 */
class H4KRegistry<ServiceId> : Registry<ServiceId> {
    private val services = mutableMapOf<ServiceId, HttpHandler>()

    override fun register(id: ServiceId, http: HttpHandler) = apply { services[id] = http }

    override fun unregister(id: ServiceId) = apply { services -= id }

    override fun lookup(id: ServiceId) = services[id] ?: throw IllegalArgumentException("$id is not registered")
}

/**
 *
 */
class RegisteringServerConfig<ServiceId>(
    private val name: ServiceId,
    private val registry: Registry<ServiceId>,
    private val serverConfig: ServerConfig) : ServerConfig {

    override fun toServer(httpHandler: HttpHandler) = object : Http4kServer {
        private val server = serverConfig.toServer(httpHandler)

        override fun start(): Http4kServer = apply {
            server.start()
            registry.register(name, httpHandler)
        }

        override fun stop(): Http4kServer = apply {
            registry.unregister(name)
            server.stop()
        }

        override fun port() = server.port()
    }
}
