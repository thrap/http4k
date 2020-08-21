package org.http4k.h4k.example.main.internal

import org.http4k.cloudnative.env.EnvironmentKey
import org.http4k.core.Credentials
import org.http4k.lens.composite

object Settings {
    val CREDEMTIALS = EnvironmentKey.composite {
        Credentials(
            defaulted("USER", "user")(it),
            defaulted("PASSWORD", "password")(it)
        )
    }
}
