package org.http4k.h4k.example.lib

import org.http4k.core.HttpHandler

interface Discovery<ServiceId> {
    /**
     * Lookup a Service HttpHandler by it's identifier
     */
    fun lookup(id: ServiceId): HttpHandler
}
