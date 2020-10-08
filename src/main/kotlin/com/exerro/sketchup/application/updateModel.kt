package com.exerro.sketchup.application

import com.exerro.sketchup.api.Event
import com.exerro.sketchup.api.data.*
import kotlin.math.pow

internal fun SketchUpModel.updateModel(message: Event): SketchUpModel = when (message) {
    is RedrawEvent -> updateViewportWindowSize(message.windowSize)
    is PointerPressEvent -> when (val interaction = findInteractionType(message.mode)) {
        InteractionType.Sketch,
        InteractionType.Navigation,
        InteractionType.SpecialNavigation,
        InteractionType.SelectSpatial,
        InteractionType.SelectTemporal,
        InteractionType.SelectMeta,
        InteractionType.Action -> application.adapter.run { handlePress(interaction, message.point) }
    }
    is PointerDragEvent -> when (message.complete) {
        true -> application.adapter.run { handleCompleteDrag(findInteractionType(message.mode), message.path) }
        else -> application.adapter.run { handlePartialDrag(findInteractionType(message.mode), message.path) }
    }
    is ScrollEvent -> when (message.mode) { // TODO: inversion of scroll mode stuff
        ScrollMode.Primary -> translateViewport(-message.delta.transformS(application.viewport.screenToWorld) * SCROLL_TRANSLATION_SCALING)
        ScrollMode.Secondary -> adjustViewportScale(message.delta.y, message.position)
    }
    is PointerMoveEvent -> copy(application = application.copy(pointer = message.position))
    else -> this
}

internal fun SketchUpModel.updateViewportWindowSize(size: Vector<ScreenSpace>) =
    copy(application = application.copy(viewport = application.viewport.copy(windowSize = size)))

internal fun SketchUpModel.translateViewport(translation: Vector<WorldSpace>) =
    copy(application = application.copy(viewport = application.viewport.copy(focus = application.viewport.focus.copy(centre = application.viewport.focus.centre + translation))))

internal fun SketchUpModel.adjustViewportScale(adjustment: Double, centre: Vector<ScreenSpace>): SketchUpModel {
    val delta = centre.transform(application.viewport.screenToWorld) - application.viewport.focus.centre
    val deltaLenScale = 2.0.pow(adjustment)

    return copy(application = application.copy(viewport = application.viewport.copy(focus = application.viewport.focus.copy(
        scale = application.viewport.focus.scale + adjustment,
        centre = application.viewport.focus.centre + delta * (deltaLenScale - 1)
    ))))
}

internal fun SketchUpModel.findInteractionType(mode: PointerMode) =
    application.pointerMappings[mode] ?: InteractionType.Sketch

private const val SCROLL_TRANSLATION_SCALING = 64.0
