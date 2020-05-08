package org.http4k.cloudnative.env

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import com.natpryce.hamkrest.throws
import org.junit.jupiter.api.Test

class PortTest {

    @Test
    fun `random port value`() {
        assertThat(Port.RANDOM, equalTo(Port(0)))
    }

    @Test
    fun `max port value`() {
        Port(65535)
        assertThat({ Port(65536) }, throws<IllegalArgumentException>())
    }

    @Test
    fun `toString is value of port`() {
        assertThat(Port(12345).toString(), equalTo("12345"))
    }
}
