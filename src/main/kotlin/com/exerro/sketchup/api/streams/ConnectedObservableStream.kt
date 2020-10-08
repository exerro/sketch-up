package com.exerro.sketchup.api.streams

interface ConnectedObservableStream<out T>: ObservableStream<T>, ObservableStreamConnection
