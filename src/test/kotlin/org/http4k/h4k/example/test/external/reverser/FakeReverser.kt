package org.http4k.h4k.example.test.external.reverser

import org.http4k.core.HttpHandler
import org.http4k.core.Response
import org.http4k.core.Status.Companion.OK
import org.http4k.core.then
import org.http4k.events.Events
import org.http4k.h4k.example.main.external.reverser.ID
import org.http4k.h4k.example.main.external.reverser.Reverser
import org.http4k.h4k.example.main.internal.RunningServerInfra
import org.http4k.h4k.example.main.internal.asAppServer
import org.http4k.h4k.example.test.external.FakeServerStack

fun Reverser.Companion.Domain() = object : Reverser {
    override fun invoke(p1: String): String = p1.reversed()
}

fun Reverser.Companion.App(events: Events, reverser: Reverser): HttpHandler = FakeServerStack(events).then {
    Response(OK).body(reverser(it.bodyString()))
}

fun main() {
    RunningServerInfra(Reverser.ID).asAppServer { Reverser.App(events, Reverser.Domain()) }.start()
}
