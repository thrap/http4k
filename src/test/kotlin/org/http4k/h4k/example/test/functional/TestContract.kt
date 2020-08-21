package org.http4k.h4k.example.test.functional

import com.natpryce.hamkrest.and
import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import org.http4k.core.HttpHandler
import org.http4k.core.Method.POST
import org.http4k.core.Request
import org.http4k.core.Status.Companion.OK
import org.http4k.hamkrest.hasBody
import org.http4k.hamkrest.hasStatus
import org.junit.jupiter.api.Test

abstract class TestContract {
    val env = TestEnvironment()
    abstract val app: HttpHandler

    @Test
    fun `test proxying e2e`() {
        assertThat(
            app(Request(POST, "").body("hello there")),
            hasStatus(OK).and(hasBody("dlrow ollehdlrow olleh"))
        )
    }

    @Test
    fun `logs calls`() {
        app(Request(POST, "").body("hello there"))
        assertThat(env.events.toList().size, equalTo(2))
    }
}
