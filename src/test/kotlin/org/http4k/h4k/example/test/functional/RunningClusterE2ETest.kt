package org.http4k.h4k.example.test.functional

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach

class RunningClusterE2ETest : TestContract() {

    @BeforeEach
    fun start() {
        env.start()
    }

    @AfterEach
    fun stop() {
        env.stop()
    }

    override val app = env.remoteClient()
}
