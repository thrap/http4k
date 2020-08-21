package org.http4k.h4k.example.test.functional

class InMemoryE2ETest : InMemoryHttpTestContract() {
    override val app = env.client()
}
