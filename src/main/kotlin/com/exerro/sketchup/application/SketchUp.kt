package com.exerro.sketchup.application

import com.exerro.sketchup.api.data.*
import com.exerro.sketchup.api.streams.ObservableStreamConnection
import com.exerro.sketchup.impl.LocalDebugSketchHost
import com.exerro.sketchup.impl.NanoVGRenderer
import com.exerro.sketchup.impl.createGLFWWindowSystem

internal fun main() {
    val windowing = createGLFWWindowSystem()
    val events = windowing.createWindow(WindowSettings())
    val renderer = NanoVGRenderer.create()
    val connections = mutableListOf<ObservableStreamConnection>()
    val model = SketchUpModel(
        SketchModel.fromHost(LocalDebugSketchHost),
        ClientModel(),
        ApplicationModel.new,
    )
    val models = events.eventTransformedFold(model, SketchUpModel::updateModel)

    connections.add(models)
    connections.add(models.connect(renderer::drawModel))
    windowing.runBlocking()
    connections.forEach(ObservableStreamConnection::disconnect)
}
