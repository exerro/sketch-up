package com.exerro.sketchup

import org.lwjgl.glfw.GLFW
import org.lwjgl.glfw.GLFW.glfwDestroyWindow

data class SketchUpModel(
    val entities: EntitySet,
    val viewport: Viewport,
    val activePath: Path<ScreenSpace>,
) {
    companion object {
        val blank = SketchUpModel(
            BasicEntitySetImpl.empty,
            Viewport(WorldPosition(0.0, 0.0), 0.0, WindowSize(0.0, 0.0)),
            Path.empty(),
        )
    }
}

typealias SketchUpMessage = WindowEvent

////////////////////////////////////////////////////////////////////////////////

fun updateModel(model: SketchUpModel, message: SketchUpMessage): SketchUpModel = when (message) {
    is RedrawEvent -> model.copy(viewport = model.viewport.copy(
        windowSize = WindowSize(message.width.toDouble(), message.height.toDouble())
    ))
    is PointerPressEvent -> {
        val position = model.viewport.inverseTransform(message.position)
        val size = model.viewport.inverseTransform(Scalar(8.0))
        model.copy(entities = model.entities.add(PointEntity(Point(position, size), Colour.blue)))
    }
    is PointerDragEvent -> when {
        message.ongoing -> model.copy(
            activePath = model.activePath + Path.of(
                position = message.drag.lastPosition,
                size = Scalar(3.0)
            )
        )
        else -> model.copy(
            entities = model.entities.add(PathEntity(model.viewport.inverseTransform(model.activePath), Colour.red)),
            activePath = Path.empty()
        )
    }
    is ScrollEvent -> when (message.mode) {
        ScrollMode.Primary -> model.copy(
            viewport = model.viewport.copy(centre = model.viewport.centre - model.viewport.inverseTransformSize(message.delta * model.viewport.windowSize / 10.0))
        )
        ScrollMode.Secondary -> model.copy(
            viewport = model.viewport.copy(scale = model.viewport.scale + message.delta.y)
        )
    }
    is KeyEvent -> {
        model
    }
    is InputEvent -> model
}

fun Renderer.drawModel(model: SketchUpModel) = draw {
    model.entities.allVisible(model.viewport.toBoundingArea(), model.viewport.windowSize.x).forEach {
        it.run { draw(model.viewport) }
    }
    ipath(model.activePath, Colour.lighterGrey)
}

////////////////////////////////////////////////////////////////////////////////

fun main() {
    initialiseGLFW()
    val windowID = glfwCreateWindow(1080, 720, "Sketch-Up")
    val vg = initialiseNanoVG()
    val events = glfwHookEvents(windowID)
    val renderer = Renderer.create(windowID, vg)
    val connections = mutableListOf<ObservableStreamConnection>()
    val models = events.fold(SketchUpModel.blank, ::updateModel)

    connections.add(models)
    connections.add(models.connect(renderer::drawModel))

    while (!GLFW.glfwWindowShouldClose(windowID)) {
        GLFW.glfwWaitEventsTimeout(0.02)
    }

    connections.forEach(ObservableStreamConnection::disconnect)
    glfwDestroyWindow(windowID)
    terminateGLFW()
}
