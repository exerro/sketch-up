package com.exerro.sketchup.application

import com.exerro.sketchup.api.Plugin
import com.exerro.sketchup.api.SketchHost
import com.exerro.sketchup.api.Window
import com.exerro.sketchup.api.data.Colour
import com.exerro.sketchup.api.data.WindowSettings
import com.exerro.sketchup.api.streams.ObservableStreamConnection
import com.exerro.sketchup.impl.LocalDebugSketchHost
import com.exerro.sketchup.impl.createGLFWWindowSystem

internal fun main() {
    val windowing = createGLFWWindowSystem()
    val window = windowing.createWindow(WindowSettings())
    val connection = launchHost(window, LocalDebugSketchHost, emptySet())
    windowing.runBlocking()
    connection.disconnect()
}

internal fun launchHost(window: Window, host: SketchHost, startupPlugins: Set<Plugin<*>>): ObservableStreamConnection {
    val connections = mutableListOf<ObservableStreamConnection>()
    val events = window.events
    val plugins = PluginManager(events, window)

    startupPlugins.forEach { plugins.load(host, it) }

    val model = SketchUpModel(
        SketchModel.fromHost(host),
        ClientModel(Colour.cyan),
        ApplicationModel.new,
    )
    val models = events.eventTransformedFold(model, SketchUpModel::updateModel)

    connections.add(models)
    connections.add(models.connect { m -> window.draw { drawModel(m) } })

    return ObservableStreamConnection.all(connections)
}
