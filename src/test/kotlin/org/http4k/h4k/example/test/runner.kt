package org.http4k.h4k.example.test

import org.http4k.client.OkHttp
import org.http4k.cloudnative.env.Port
import org.http4k.core.HttpHandler
import org.http4k.core.Method.GET
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status.Companion.OK
import org.http4k.core.Uri
import org.http4k.core.then
import org.http4k.filter.ClientFilters
import org.http4k.h4k.example.lib.H4KCluster
import org.http4k.h4k.example.main.App
import org.http4k.h4k.example.main.Doubler
import org.http4k.h4k.example.main.ExternalServiceId
import org.http4k.h4k.example.main.InternalServiceId
import org.http4k.h4k.example.main.Proxy
import org.http4k.h4k.example.main.Reverser

fun FakeReverserApp(): HttpHandler = { req: Request -> Response(OK).body(req.bodyString().reversed()) }
fun FakeDoublerApp(): HttpHandler = { req: Request -> Response(OK).body(req.bodyString() + req.bodyString()) }

fun main() {
    // this is our "fakes" cluster
    val egress = H4KCluster<ExternalServiceId>()
        .install(Reverser.ID) { FakeReverserApp() }
        .expose(Doubler.ID, Port(20000))
        .install(Doubler.ID) { FakeDoublerApp() }
        .expose(Reverser.ID, Port(10000))
        .start()

    // this is our service cluster
    val cluster = H4KCluster<InternalServiceId>()
        .install(App.ID) { App(egress) }
        .install(Proxy.ID) { Proxy(it) }
        .expose(Proxy.ID, Port(8000))
        .start()

    // look up the service HttpHandler by ID
    println(cluster.lookup(Proxy.ID)(Request(GET, "")))

    // because we've exposed it, we can also go over the wire
    val client = ClientFilters.SetBaseUriFrom(Uri.of("http://localhost:8000")).then(OkHttp())
    println(client(Request(GET, "")))

    cluster.stop()
    egress.stop()
}
