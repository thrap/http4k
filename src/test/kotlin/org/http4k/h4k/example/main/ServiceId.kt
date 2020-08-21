package org.http4k.h4k.example.main

sealed class ServiceId(val name: String) {
    override fun toString() = name
}

class InternalServiceId(name: String) : ServiceId(name)
class ExternalServiceId(name: String) : ServiceId(name)
