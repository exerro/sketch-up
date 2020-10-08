package com.exerro.sketchup.api

import com.exerro.sketchup.api.data.ExtendedWindowEvent
import com.exerro.sketchup.api.data.WindowSettings
import com.exerro.sketchup.api.streams.ConnectedObservableStream

interface WindowSystem {
    /** Create a window and return its event stream. */
    fun createWindow(settings: WindowSettings): ConnectedObservableStream<ExtendedWindowEvent>
    /** Keep windows running and alive until none are open, then clean up. */
    fun runBlocking()
}
