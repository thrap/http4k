package org.http4k.h4k.example.test.functional

class InMemoryE2ETest : TestContract() {
    override val app = env.client()
}
