package org.http4k.h4k.example.test.main

import org.http4k.cloudnative.env.Environment
import org.http4k.h4k.example.lib.Discovery
import org.http4k.h4k.example.main.ExternalServiceId
import org.http4k.h4k.example.main.InternalServiceId
import org.http4k.h4k.example.main.ServerInfra

class TestServerInfra(private val env: Environment) : Environment by env, ServerInfra {
    override val events = TestEvents()

    override val internalDiscovery: Discovery<InternalServiceId>
        get() = TODO("Not yet implemented")

    override val externalDiscovery: Discovery<ExternalServiceId>
        get() = TODO("Not yet implemented")
}
