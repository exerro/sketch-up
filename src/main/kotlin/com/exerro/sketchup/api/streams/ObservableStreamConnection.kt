package com.exerro.sketchup.api.streams

fun interface ObservableStreamConnection {
    fun disconnect()

    companion object {
        fun all(connections: Iterable<ObservableStreamConnection>) = ObservableStreamConnection {
            connections.forEach(ObservableStreamConnection::disconnect)
        }
    }
}
