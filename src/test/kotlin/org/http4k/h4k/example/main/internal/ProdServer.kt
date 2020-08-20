package org.http4k.h4k.example.main.internal

import org.http4k.core.HttpHandler
import org.http4k.server.Http4kServer
import org.http4k.server.SunHttp
import org.http4k.server.asServer

fun ProdAppServer(toApp: () -> HttpHandler): Http4kServer = toApp().asServer(SunHttp(10000))
