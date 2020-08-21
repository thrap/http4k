package org.http4k.h4k.example.test.functional

import com.natpryce.hamkrest.and
import com.natpryce.hamkrest.assertion.assertThat
import org.http4k.core.Method.POST
import org.http4k.core.Request
import org.http4k.core.Status
import org.http4k.core.then
import org.http4k.filter.ClientFilters.BasicAuth
import org.http4k.h4k.example.main.internal.Proxy
import org.http4k.h4k.example.main.internal.Settings.CREDEMTIALS
import org.http4k.hamkrest.hasBody
import org.http4k.hamkrest.hasStatus
import org.junit.jupiter.api.Test

class InMemoryE2ETest {
    private val testEnv = TestEnvironment()

    @Test
    fun `test proxying e2e`() {
        val app = BasicAuth(CREDEMTIALS(testEnv.env)).then(testEnv.lookup(Proxy.ID))
        assertThat(app(Request(POST, "").body("hello there")), hasStatus(Status.OK).and(hasBody("dlrow ollehdlrow olleh")))
    }
}
