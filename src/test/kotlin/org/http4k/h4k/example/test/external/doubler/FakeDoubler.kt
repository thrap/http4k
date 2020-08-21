package org.http4k.h4k.example.test.external.doubler

import org.http4k.core.HttpHandler
import org.http4k.core.Response
import org.http4k.core.Status.Companion.OK
import org.http4k.h4k.example.main.external.doubler.Doubler
import org.http4k.h4k.example.main.external.doubler.ID
import org.http4k.h4k.example.main.internal.RunningServerInfra
import org.http4k.h4k.example.main.internal.asAppServer

fun Doubler.Companion.Domain() = object : Doubler {
    override fun invoke(p1: String): String = p1 + p1
}

fun Doubler.Companion.App(doubler: Doubler): HttpHandler = {
    Response(OK).body(doubler(it.bodyString()))
}

fun main() {
    RunningServerInfra(Doubler.ID).asAppServer { Doubler.App(Doubler.Domain()) }.start()
}
