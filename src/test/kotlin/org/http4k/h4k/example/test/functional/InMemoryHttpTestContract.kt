package org.http4k.h4k.example.test.functional

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import org.http4k.core.HttpHandler
import org.http4k.h4k.example.main.external.doubler.Doubler
import org.http4k.h4k.example.main.external.reverser.Reverser
import org.http4k.h4k.example.main.internal.backend.Backend
import org.http4k.h4k.example.main.internal.backend.Domain
import org.http4k.h4k.example.main.internal.gateway.Domain
import org.http4k.h4k.example.main.internal.gateway.Gateway
import org.http4k.h4k.example.test.external.doubler.Domain
import org.http4k.h4k.example.test.external.reverser.Domain
import org.http4k.h4k.example.test.main.TestEvents
import org.junit.jupiter.api.Test

abstract class TestContract {
    abstract val gateway: Gateway
    abstract val events: TestEvents

    @Test
    fun `can mess with strings`() {
        assertThat(gateway("hello world"), equalTo("dlrow ollehdlrow olleh"))
    }
}

abstract class InMemoryHttpTestContract : TestContract() {
    val env = TestEnvironment()
    abstract val app: HttpHandler

    override val gateway by lazy { Gateway.Http(app) }
    override val events = env.events

    @Test
    fun `logs calls`() {
        gateway("hello there")
        assertThat(events.toList().size, equalTo(4))
    }
}

class DomainTestContract : TestContract() {
    override val gateway by lazy { Gateway.Domain(Reverser.Domain(), Backend.Domain(Doubler.Domain())) }
    override val events = TestEvents()
}
