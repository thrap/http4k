package org.http4k.h4k.example.test

import org.http4k.h4k.example.main.internal.backend.main as runBackend
import org.http4k.h4k.example.main.internal.gateway.main as runGateway
import org.http4k.h4k.example.test.external.doubler.main as runDoubler
import org.http4k.h4k.example.test.external.reverser.main as runReverser


/**
 * The problem here is that we end up having to mess around with a bunch of ports to get everything running correctly...
 */
fun main() {
    runBackend()
    runGateway()
    runDoubler()
    runReverser()
}
