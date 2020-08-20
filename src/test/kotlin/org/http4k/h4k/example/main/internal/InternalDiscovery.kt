package org.http4k.h4k.example.main.internal

import org.http4k.client.OkHttp
import org.http4k.core.Uri
import org.http4k.core.then
import org.http4k.filter.ClientFilters
import org.http4k.h4k.example.lib.Discovery
import org.http4k.h4k.example.main.InternalServiceId

class InternalDiscovery : Discovery<InternalServiceId> {
    override fun lookup(id: InternalServiceId) = ClientFilters.SetHostFrom(Uri.of(id.name)).then(OkHttp())
}
