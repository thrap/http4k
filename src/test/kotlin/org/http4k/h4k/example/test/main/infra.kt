package org.http4k.h4k.example.test.main

import org.http4k.events.Event
import org.http4k.events.Events

fun TestEvents() = object : Events {
    val recorded = mutableListOf<Event>()
    override fun invoke(p1: Event) {
        recorded.add(p1)
    }
}
