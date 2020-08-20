package org.http4k.h4k.example.test

import org.http4k.h4k.example.main.internal.Main
import org.http4k.h4k.example.main.internal.Proxy
import org.http4k.h4k.example.test.external.FakeDoubler
import org.http4k.h4k.example.test.external.FakeReverser

/**
 * The problem here is that we end up having to mess around with a bunch of ports to get everything running correctly...
 */
fun main() {
    Proxy.main()
    Main.main()
    FakeReverser.main()
    FakeDoubler.main()
}
