package org.http4k.cloudnative.env

data class Port(val value: Int) {
    init {
        require(value <= 65535) { "Out of range Port: '$value'" }
    }

    override fun toString() = value.toString()

    companion object {
        val RANDOM = Port(0)
    }
}
