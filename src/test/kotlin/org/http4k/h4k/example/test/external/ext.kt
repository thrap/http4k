package org.http4k.h4k.example.test.external

import org.http4k.events.Events
import org.http4k.h4k.example.main.IncomingHttpRequest
import org.http4k.h4k.example.main.RecordRequest

fun FakeServerStack(events: Events) = RecordRequest(events, ::IncomingHttpRequest)
