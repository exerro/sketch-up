package com.exerro.sketchup.application

import com.exerro.sketchup.api.VisualHint
import com.exerro.sketchup.api.Entity
import com.exerro.sketchup.api.Event
import com.exerro.sketchup.api.data.*
import com.exerro.sketchup.api.util.path
import com.exerro.sketchup.api.util.rectangularSelection
import com.exerro.sketchup.util.boundingArea

internal fun SketchUpModel.updateModel(message: Event): SketchUpModel = when (message) {
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
    else -> this
}

internal fun SketchUpModel.updateViewportWindowSize(size: Vector<ScreenSpace>) =
    copy(application = application.copy(viewport = application.viewport.copy(windowSize = size)))

internal fun SketchUpModel.translateViewport(translation: Vector<WorldSpace>) =
    copy(application = application.copy(viewport = application.viewport.copy(focus = application.viewport.focus.copy(centre = application.viewport.focus.centre + translation))))

internal fun SketchUpModel.adjustViewportScale(adjustment: Double) =
    copy(application = application.copy(viewport = application.viewport.copy(focus = application.viewport.focus.copy(scale = application.viewport.focus.scale + adjustment))))

internal fun SketchUpModel.setPathHint(path: Path<ScreenSpace>) =
    copy(application = application.copy(visualHint = VisualHint.path(path))).deselect()

internal fun SketchUpModel.setSelectionHint(first: PathPoint<ScreenSpace>, second: PathPoint<ScreenSpace>) =
    copy(application = application.copy(visualHint = VisualHint.rectangularSelection((Path.of(first) + Path.of(second)).boundingArea))).deselect()

internal fun SketchUpModel.addEntity(entity: Entity) = copy(
    sketch = sketch.copy(snapshot = sketch.snapshot.copy(entities = sketch.snapshot.entities.add(entity))),
    application = application.copy(lastAddedEntity = entity),
).deselect()

internal fun SketchUpModel.deselect() =
    copy(application = application.copy(selectedEntities = emptySet()))

private const val SCROLL_TRANSLATION_SCALING = 64.0
private const val ZOOM_SCALING = 8.0
