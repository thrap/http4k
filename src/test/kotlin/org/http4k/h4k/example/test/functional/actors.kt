package org.http4k.h4k.example.test.functional

import com.natpryce.hamkrest.assertion.assertThat
import org.http4k.core.HttpHandler
import org.http4k.core.Method.POST
import org.http4k.core.Request
import org.http4k.core.Status.Companion.OK
import org.http4k.h4k.example.main.internal.gateway.Gateway
import org.http4k.hamkrest.hasStatus

fun Gateway.Companion.Http(http: HttpHandler) = object : Gateway {
    override fun invoke(p1: String): String {
        val response = http(Request(POST, "").body(p1))
        assertThat(response, hasStatus(OK))
        return response.bodyString()
    }
}
