package org.http4k.h4k.example.test.external.doubler

import org.http4k.core.HttpHandler
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status.Companion.OK
import org.http4k.h4k.example.main.external.doubler.Doubler
import org.http4k.h4k.example.main.external.doubler.ID
import org.http4k.h4k.example.main.internal.RunningServerInfra
import org.http4k.h4k.example.main.internal.asAppServer

class FakeDoubler : HttpHandler {
    override fun invoke(p1: Request) = Response(OK).body(p1.bodyString() + p1.bodyString())
}

fun main() {
    RunningServerInfra(Doubler.ID).asAppServer { FakeDoubler() }.start()
}
