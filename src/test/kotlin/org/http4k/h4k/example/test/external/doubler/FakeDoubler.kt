package org.http4k.h4k.example.test.external.doubler

import org.http4k.core.HttpHandler
import org.http4k.core.Response
import org.http4k.core.Status.Companion.OK
import org.http4k.core.then
import org.http4k.events.Events
import org.http4k.events.then
import org.http4k.h4k.example.main.EventStack
import org.http4k.h4k.example.main.external.doubler.Doubler
import org.http4k.h4k.example.main.external.doubler.ID
import org.http4k.h4k.example.main.internal.RunningServerInfra
import org.http4k.h4k.example.main.internal.asAppServer
import org.http4k.h4k.example.test.external.FakeServerStack

fun Doubler.Companion.Domain() = object : Doubler {
    override fun invoke(p1: String): String = p1 + p1
}

fun Doubler.Companion.App(events: Events, doubler: Doubler): HttpHandler =
    FakeServerStack(EventStack(ID).then(events)).then { Response(OK).body(doubler(it.bodyString())) }


fun main() {
    RunningServerInfra(Doubler.ID).asAppServer { Doubler.App(events, Doubler.Domain()) }.start()
}
