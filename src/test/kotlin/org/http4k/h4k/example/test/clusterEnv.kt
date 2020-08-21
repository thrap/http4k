package org.http4k.h4k.example.test

import org.http4k.client.OkHttp
import org.http4k.cloudnative.env.Port
import org.http4k.core.Method.GET
import org.http4k.core.Request
import org.http4k.core.Uri
import org.http4k.core.then
import org.http4k.filter.ClientFilters
import org.http4k.h4k.example.lib.H4KCluster
import org.http4k.h4k.example.main.ExternalServiceId
import org.http4k.h4k.example.main.InternalServiceId
import org.http4k.h4k.example.main.external.Doubler
import org.http4k.h4k.example.main.external.Reverser
import org.http4k.h4k.example.main.internal.Main
import org.http4k.h4k.example.main.internal.Proxy
import org.http4k.h4k.example.test.external.FakeDoubler
import org.http4k.h4k.example.test.external.FakeReverser

fun main() {
    // this is our "fakes" cluster
    val externals = H4KCluster<ExternalServiceId>()
        .install(Reverser.ID, Port(20000)) { FakeReverser() }
        .install(Doubler.ID, Port(10000)) { FakeDoubler() }
        .start()

    // this is our service cluster
    val cluster = H4KCluster<InternalServiceId>()
        .install(Main.ID) { Main(externals) }
        .install(Proxy.ID, Port(8000)) { Proxy(it) }
        .start()

    // look up the service HttpHandler by ID
    println(cluster.lookup(Proxy.ID)(Request(GET, "")))

    // because we've exposed it, we can also go over the wire
    val client = ClientFilters.SetBaseUriFrom(Uri.of("http://localhost:8000")).then(OkHttp())
    println(client(Request(GET, "")))

    cluster.stop()
    externals.stop()
}
