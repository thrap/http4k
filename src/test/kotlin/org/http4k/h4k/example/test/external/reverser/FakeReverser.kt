package org.http4k.h4k.example.test.external.reverser

import org.http4k.core.HttpHandler
import org.http4k.core.Response
import org.http4k.core.Status.Companion.OK
import org.http4k.h4k.example.main.external.doubler.Doubler
import org.http4k.h4k.example.main.external.doubler.ID
import org.http4k.h4k.example.main.external.reverser.Reverser
import org.http4k.h4k.example.main.internal.RunningServerInfra
import org.http4k.h4k.example.main.internal.asAppServer

fun Reverser.Companion.Domain() = object : Reverser {
    override fun invoke(p1: String): String = p1.reversed()
}

fun Reverser.Companion.App(reverser: Reverser): HttpHandler = {
    Response(OK).body(reverser(it.bodyString())) }

fun main() {
    RunningServerInfra(Doubler.ID).asAppServer { Reverser.App(Reverser.Domain()) }.start()
}
