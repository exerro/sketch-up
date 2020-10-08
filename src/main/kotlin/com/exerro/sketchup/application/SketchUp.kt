package com.exerro.sketchup.application

import com.exerro.sketchup.api.data.Colour
import com.exerro.sketchup.api.data.WindowSettings
import com.exerro.sketchup.api.streams.ObservableStreamConnection
import com.exerro.sketchup.impl.LocalDebugSketchHost
import com.exerro.sketchup.impl.createGLFWWindowSystem

internal fun main() {
    val windowing = createGLFWWindowSystem()
    val window = windowing.createWindow(WindowSettings())
    val connections = mutableListOf<ObservableStreamConnection>()
    val model = SketchUpModel(
        SketchModel.fromHost(LocalDebugSketchHost),
        ClientModel(Colour.cyan),
        ApplicationModel.new,
    )
    val models = window.events.eventTransformedFold(model, SketchUpModel::updateModel)

    connections.add(models)
    connections.add(models.connect { m -> window.draw { drawModel(m) } })
    windowing.runBlocking()
    connections.forEach(ObservableStreamConnection::disconnect)
}
