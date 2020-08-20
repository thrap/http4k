package org.http4k.h4k.example.test.external

import org.http4k.core.HttpHandler
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status.Companion.OK
import org.http4k.h4k.example.main.internal.ProdAppServer

object FakeReverser {
    operator fun invoke(): HttpHandler = { req: Request -> Response(OK).body(req.bodyString().reversed()) }

    @JvmStatic
    fun main() {
        ProdAppServer { FakeReverser() }.start()
    }
}
