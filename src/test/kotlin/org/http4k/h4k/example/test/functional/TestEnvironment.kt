package org.http4k.h4k.example.test.functional

import org.http4k.cloudnative.env.Environment
import org.http4k.cloudnative.env.Environment.Companion.EMPTY
import org.http4k.core.Uri
import org.http4k.core.then
import org.http4k.filter.ClientFilters.BasicAuth
import org.http4k.filter.ClientFilters.SetBaseUriFrom
import org.http4k.h4k.example.lib.H4KCluster
import org.http4k.h4k.example.main.ExternalServiceId
import org.http4k.h4k.example.main.InternalServiceId
import org.http4k.h4k.example.main.external.doubler.Doubler
import org.http4k.h4k.example.main.external.doubler.ID
import org.http4k.h4k.example.main.external.reverser.ID
import org.http4k.h4k.example.main.external.reverser.Reverser
import org.http4k.h4k.example.main.internal.Settings.CREDEMTIALS
import org.http4k.h4k.example.main.internal.backend.App
import org.http4k.h4k.example.main.internal.backend.Backend
import org.http4k.h4k.example.main.internal.backend.ID
import org.http4k.h4k.example.main.internal.gateway.App
import org.http4k.h4k.example.main.internal.gateway.Gateway
import org.http4k.h4k.example.main.internal.gateway.ID
import org.http4k.h4k.example.test.external.doubler.App
import org.http4k.h4k.example.test.external.doubler.Domain
import org.http4k.h4k.example.test.external.reverser.App
import org.http4k.h4k.example.test.external.reverser.Domain
import org.http4k.h4k.example.test.functional.TestEnvironment.fakes.doubler
import org.http4k.h4k.example.test.functional.TestEnvironment.fakes.reverser
import org.http4k.h4k.example.test.main.TestEvents

class TestEnvironment(private val env: Environment = EMPTY) : Environment by env {
    val events = TestEvents()
    val externalEvents = TestEvents()

    object fakes {
        val reverser = Reverser.Domain()
        val doubler = Doubler.Domain()
    }

    private val externalCluster = H4KCluster<ExternalServiceId>()
        .deploy(Reverser.ID) { Reverser.App(externalEvents, reverser) }
        .deploy(Doubler.ID) { Doubler.App(externalEvents, doubler) }

    private val cluster = H4KCluster<InternalServiceId>()
        .deploy(Backend.ID) { Backend.App(env, events, externalCluster) }
        .deploy(Gateway.ID) { Gateway.App(env, events, it, externalCluster) }
        .expose(Gateway.ID)

    fun start() = apply { cluster.start() }

    fun stop() = apply { cluster.stop() }

    fun remoteClient() = BasicAuth(CREDEMTIALS(env))
        .then(SetBaseUriFrom(Uri.of("http://localhost:${cluster.port()}")))
        .then(cluster.clientFor(Gateway.ID))

    fun client() = BasicAuth(CREDEMTIALS(env)).then(cluster.lookup(Gateway.ID))
}
