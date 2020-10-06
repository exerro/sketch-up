package com.exerro.sketchup

interface WindowSystem {
    /** Create a window and return its event stream. */
    fun createWindow(settings: WindowSettings): ConnectedObservableStream<ExtendedWindowEvent>
    /** Keep windows running and alive until none are open, then clean up. */
    fun runBlocking()
}
