package com.exerro.sketchup.api

import com.exerro.sketchup.api.data.WindowSettings

interface WindowSystem {
    /** Create a window and return its event stream. */
    fun createWindow(settings: WindowSettings): Window
    /** Keep windows running and alive until none are open, then clean up. */
    fun runBlocking()
}
