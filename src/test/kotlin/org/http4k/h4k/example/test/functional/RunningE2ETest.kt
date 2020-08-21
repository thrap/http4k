package org.http4k.h4k.example.test.functional

import com.natpryce.hamkrest.and
import com.natpryce.hamkrest.assertion.assertThat
import org.http4k.client.OkHttp
import org.http4k.core.Method.POST
import org.http4k.core.Request
import org.http4k.core.Status
import org.http4k.core.then
import org.http4k.filter.ClientFilters.BasicAuth
import org.http4k.h4k.example.main.internal.Settings.CREDEMTIALS
import org.http4k.hamkrest.hasBody
import org.http4k.hamkrest.hasStatus
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class RunningE2ETest {
    private val testEnv = TestEnvironment()

    @BeforeEach
    fun start() {
        testEnv.start()
    }

    @AfterEach
    fun stop() {
        testEnv.stop()
    }

    @Test
    fun `test proxying e2e`() {
        val app = BasicAuth(CREDEMTIALS(testEnv.env)).then(OkHttp())
        assertThat(app(Request(POST, "http://localhost:10000").body("hello there")), hasStatus(Status.OK).and(hasBody("dlrow ollehdlrow olleh")))
    }
}
