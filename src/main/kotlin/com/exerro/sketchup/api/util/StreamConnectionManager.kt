package com.exerro.sketchup.api.util

import com.exerro.sketchup.api.streams.ObservableStreamConnection

class StreamConnectionManager<T> {
    fun add(onItem: (T) -> Unit): ObservableStreamConnection {
        synchronized(connections) { connections.add(onItem) }
        return ObservableStreamConnection {
            synchronized(connections) { connections.remove(onItem) }
        }
    }

    fun invoke(item: T) {
        for (onItem in synchronized(connections) { connections.toList() }) {
            onItem(item)
        }
    }

    private val connections: MutableList<(T) -> Unit> = mutableListOf()
}
