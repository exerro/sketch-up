package com.exerro.sketchup.api.streams

interface ConnectedObservable<out T>: ConnectedObservableStream<T> {
    val latest: T
}
