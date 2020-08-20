package org.http4k.h4k.example.test.external

import org.http4k.core.HttpHandler
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.h4k.example.main.internal.ProdAppServer

object FakeDoubler {
    operator fun invoke(): HttpHandler = { req: Request -> Response(Status.OK).body(req.bodyString() + req.bodyString()) }

    @JvmStatic
    fun main(args: Array<String>) {
        ProdAppServer { FakeDoubler() }.start()
    }
}
