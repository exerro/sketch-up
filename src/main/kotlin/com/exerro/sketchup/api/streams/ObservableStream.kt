package com.exerro.sketchup.api.streams

fun interface ObservableStream<out T> {
    fun connect(onItem: (T) -> Unit): ObservableStreamConnection

    companion object {
        fun <T> empty() = of<T>()

        fun <T> of(vararg items: T) = ObservableStream<T> { onEvent ->
            items.forEach(onEvent)
            ObservableStreamConnection {}
        }

        fun <T> combine(vararg streams: ObservableStream<T>) = ObservableStream<T> { onEvent ->
            val connections = streams.map { it.connect(onEvent) }
            ObservableStreamConnection { connections.forEach(ObservableStreamConnection::disconnect) }
        }
    }
}

////////////////////////////////////////////////////////////////////////////////
