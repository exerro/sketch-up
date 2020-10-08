package com.exerro.sketchup.api

import com.exerro.sketchup.api.data.ExtendedWindowEvent
import com.exerro.sketchup.api.streams.ObservableStream

interface Window {
    val events: ObservableStream<ExtendedWindowEvent>

    fun close()
    fun draw(draw: DrawContext.() -> Unit)
}
