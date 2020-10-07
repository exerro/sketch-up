package com.exerro.sketchup

import com.exerro.sketchup.impl.NanoVGRenderer
import com.exerro.sketchup.impl.createGLFWWindowSystem
import com.exerro.sketchup.impl.initialiseNanoVG
import com.exerro.sketchup.util.*

fun main() {
    val windowing = createGLFWWindowSystem()
    val events = windowing.createWindow(WindowSettings())
    val vg = initialiseNanoVG()
    val renderer = NanoVGRenderer.create(vg)
    val connections = mutableListOf<ObservableStreamConnection>()
    val models = events.eventTransformedFold(SketchUpModel.new, SketchUpModel::updateModel)

    connections.add(models)
    connections.add(models.connect(renderer::drawModel))
    windowing.runBlocking()
    connections.forEach(ObservableStreamConnection::disconnect)
}

fun SketchUpModel.updateModel(message: SketchUpMessage): SketchUpModel = when (message) {
    is RedrawEvent -> updateViewportWindowSize(message.windowSize)
    is PointerPressEvent -> when (message.mode) {
        PointerMode.Primary -> application.adapter.run { handlePrimaryPointerPress(message.alternate, message.point) }
        PointerMode.Tertiary -> application.adapter.run { handleSecondaryPointerPress(message.alternate, message.point) }
        PointerMode.Secondary -> application.adapter.run { handleTertiaryPointerPress(message.alternate, message.point) }
    }
    is PointerDragEvent -> when (message.complete) {
        true -> when (message.mode) {
            PointerMode.Primary -> application.adapter.run { completePrimaryPointerDrag(message.alternate, message.path) }
            PointerMode.Secondary -> application.adapter.run { completeSecondaryPointerDrag(message.alternate, message.path) }
            else -> when (message.alternate) {
                true -> {
                    val delta = message.path.offset.transformS(application.viewport.screenToWorld)
                    adjustViewportScale(delta.y * ZOOM_SCALING)
                }
                else -> translateViewport(-message.path.offset.transformS(application.viewport.screenToWorld))
            }
        }
        else -> when (message.mode) {
            PointerMode.Tertiary -> when (message.alternate) {
                true -> {
                    val delta = message.path.offset.transformS(application.viewport.screenToWorld)
                    adjustViewportScale(delta.y * ZOOM_SCALING)
                }
                else -> translateViewport(-message.path.offset.transformS(application.viewport.screenToWorld))
            }
            else -> application.adapter.run { handlePointerDrag(message.mode, message.alternate, message.path) }
        }
    }
    is ScrollEvent -> when (message.mode) {
        ScrollMode.Primary -> translateViewport(-message.delta.transformS(application.viewport.screenToWorld) * SCROLL_TRANSLATION_SCALING)
        ScrollMode.Secondary -> adjustViewportScale(message.delta.y)
    }
    is PointerMoveEvent -> copy(application = application.copy(pointer = message.position))
    is KeyEvent, is InputEvent -> this
}

private const val SCROLL_TRANSLATION_SCALING = 64.0
private const val ZOOM_SCALING = 8.0
