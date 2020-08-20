package org.http4k.h4k.example.test

import org.http4k.h4k.example.main.external.Doubler
import org.http4k.h4k.example.main.external.Reverser
import org.http4k.h4k.example.main.internal.Main
import org.http4k.h4k.example.main.internal.Proxy
import org.http4k.h4k.example.test.external.main

fun main() {
    Proxy.main()
    Main.main()
    Reverser.main()
    Doubler.main()
}
