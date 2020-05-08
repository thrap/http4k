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

typealias ServerControl = (() -> Http4kServer) -> Unit

interface Registry<ServiceId> : Discovery<ServiceId> {
    fun register(id: ServiceId, http: HttpHandler): ServerControl
    fun unregister(id: ServiceId)
}

/**
 * Use this when running in deployed K8S cluster to register and lookup K8S services
 */
class K8SServiceRegistry<ServiceId>(private val port: (ServiceId) -> Port = { Port(8080) }) : Registry<ServiceId> {

    private val services = mutableMapOf<ServiceId, Http4kServer>()

    override fun register(id: ServiceId, http: HttpHandler): ServerControl = {
        services[id] = it().start()
    }

    override fun unregister(id: ServiceId) {
        services[id]?.stop()
    }

    override fun lookup(id: ServiceId) = ClientFilters.SetHostFrom(Uri.of("http://${id}:${port(id).value}")).then(OkHttp())
}

/**
 * Use this in locally running cluster mode (all port bound services)
 */
class LocalRegistry<ServiceId> : Registry<ServiceId> {

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

/**
 * Use this for an entirely in-memory cluster
 */
class H4KRegistry<ServiceId> : Registry<ServiceId> {
    private val services = mutableMapOf<ServiceId, HttpHandler>()

    override fun register(id: ServiceId, http: HttpHandler): ServerControl = {
        services[id] = http
    }

    override fun unregister(id: ServiceId) {
        services -= id
    }

    override fun lookup(id: ServiceId) = services[id] ?: throw IllegalArgumentException("$id is not registered")
}

class RegisteringServerConfig<ServiceId>(
    private val name: ServiceId,
    private val registry: Registry<ServiceId>,
    private val serverConfig: () -> ServerConfig) : ServerConfig {

    override fun toServer(httpHandler: HttpHandler) = object : Http4kServer {
        override fun start() = apply {
            registry.register(name, httpHandler).invoke { serverConfig().toServer(httpHandler) }
        }

        override fun stop() = apply {
            registry.unregister(name)
        }

        override fun port() = serverConfig().toServer(httpHandler).port()
    }
}
