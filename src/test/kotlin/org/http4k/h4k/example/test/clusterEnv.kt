package org.http4k.h4k.example.test

//
//fun main() {
//
//    // this is our "fakes" cluster
//    val externals = H4KCluster<ExternalServiceId>()
//        .install(Reverser.ID, Port(20000)) { FakeReverser() }
//        .install(Doubler.ID, Port(10000)) { FakeDoubler() }
//        .start()
//
//    // this is our service cluster
//    val cluster = H4KCluster<InternalServiceId>()
//        .install(Main.ID) { Main(externals) }
//        .install(Proxy.ID, Port(8000)) { Proxy(it) }
//        .start()
//
//    // look up the service HttpHandler by ID
//    println(cluster.lookup(Proxy.ID)(Request(GET, "")))
//
//    // because we've exposed it, we can also go over the wire
//    val client = ClientFilters.SetBaseUriFrom(Uri.of("http://localhost:8000")).then(OkHttp())
//    println(client(Request(GET, "")))
//
//    cluster.stop()
//    externals.stop()
//}
