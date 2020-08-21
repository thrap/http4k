package org.http4k.h4k.example.test.functional

import org.http4k.cloudnative.env.Environment
import org.http4k.cloudnative.env.Environment.Companion.EMPTY
import org.http4k.core.Uri
import org.http4k.core.then
import org.http4k.filter.ClientFilters
import org.http4k.filter.ClientFilters.BasicAuth
import org.http4k.h4k.example.lib.H4KCluster
import org.http4k.h4k.example.main.ExternalServiceId
import org.http4k.h4k.example.main.InternalServiceId
import org.http4k.h4k.example.main.external.Doubler
import org.http4k.h4k.example.main.external.Reverser
import org.http4k.h4k.example.main.internal.Main
import org.http4k.h4k.example.main.internal.Proxy
import org.http4k.h4k.example.main.internal.Settings.CREDEMTIALS
import org.http4k.h4k.example.test.external.FakeDoubler
import org.http4k.h4k.example.test.external.FakeReverser
import org.http4k.h4k.example.test.functional.TestEnvironment.fakes.doubler
import org.http4k.h4k.example.test.functional.TestEnvironment.fakes.reverser
import org.http4k.h4k.example.test.main.TestEvents

class TestEnvironment(private val env: Environment = EMPTY) : Environment by env {
    val events = TestEvents()

    object fakes {
        val reverser = FakeReverser()
        val doubler = FakeDoubler()
    }

    private val externalCluster = H4KCluster<ExternalServiceId>()
        .deploy(Reverser.ID) { reverser }
        .deploy(Doubler.ID) { doubler }

    private val cluster = H4KCluster<InternalServiceId>()
        .deploy(Main.ID) { Main(env, events, externalCluster) }
        .deploy(Proxy.ID) { Proxy(env, events, it) }
        .expose(Proxy.ID)

    fun start() = apply { cluster.start() }

    fun stop() = apply { cluster.stop() }

    fun remoteClient() = BasicAuth(CREDEMTIALS(env))
        .then(ClientFilters.SetBaseUriFrom(Uri.of("http://localhost:${cluster.port()}")))
        .then(cluster.clientFor(Proxy.ID))

    fun client() = BasicAuth(CREDEMTIALS(env)).then(cluster.lookup(Proxy.ID))
}
