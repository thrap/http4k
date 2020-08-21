package org.http4k.h4k.example.test

import org.http4k.client.OkHttp
import org.http4k.cloudnative.env.Port
import org.http4k.core.HttpHandler
import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.core.Uri
import org.http4k.core.then
import org.http4k.filter.ClientFilters
import org.http4k.h4k.example.lib.H4KCluster
import org.http4k.h4k.example.main.App
import org.http4k.h4k.example.main.ExternalServiceId
import org.http4k.h4k.example.main.InternalServiceId
import org.http4k.h4k.example.main.Proxy
import org.http4k.h4k.example.main.Reverser

fun FakeReverserApp(): HttpHandler = { req: Request -> Response(Status.OK).body(req.bodyString().reversed()) }

fun main() {
    // this is our "fakes" cluster
    val egress = H4KCluster<ExternalServiceId>()
        .install(Reverser.ID) { FakeReverserApp() }
        .expose(Reverser.ID, Port(10000))
        .start()

    // this is our service cluster
    val cluster = H4KCluster<InternalServiceId>()
        .install(App.ID) { App(egress.lookup(Reverser.ID)) }
        .install(Proxy.ID) { discovery -> Proxy(discovery.lookup(App.ID)) }
        .expose(Proxy.ID, Port(8000))
        .start()

    // look up the service HttpHandler by ID
    println(cluster.lookup(Proxy.ID)(Request(Method.GET, "")))

    // because we've exposed it, we can also go over the wire
    val client = ClientFilters.SetBaseUriFrom(Uri.of("http://localhost:8000")).then(OkHttp())
    println(client(Request(Method.GET, "")))

    cluster.stop()
    egress.stop()
}
