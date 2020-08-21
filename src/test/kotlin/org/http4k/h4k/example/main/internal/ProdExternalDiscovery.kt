package org.http4k.h4k.example.main.internal

import org.http4k.client.OkHttp
import org.http4k.cloudnative.env.Environment
import org.http4k.cloudnative.env.EnvironmentKey
import org.http4k.core.then
import org.http4k.filter.ClientFilters.SetBaseUriFrom
import org.http4k.h4k.example.lib.Discovery
import org.http4k.h4k.example.main.ExternalServiceId
import org.http4k.lens.uri

/**
 * The environment requires external services to have their base URLs registered with the key format <ID>_URL
 */
class ProdExternalDiscovery(private val env: Environment) : Discovery<ExternalServiceId> {
    override fun lookup(id: ExternalServiceId) = SetBaseUriFrom(id.uriKey()(env)).then(OkHttp())
}

private fun ExternalServiceId.uriKey() = EnvironmentKey.uri().required(name.toUpperCase() + "_URL")
