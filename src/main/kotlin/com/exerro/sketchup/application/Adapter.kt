package com.exerro.sketchup.application

import com.exerro.sketchup.api.data.*

internal interface Adapter {
    fun SketchUpModel.handlePress(interaction: InteractionType, point: Point<ScreenSpace>) = this
    fun SketchUpModel.handlePartialDrag(interaction: InteractionType, path: Path<ScreenSpace>) = this
    fun SketchUpModel.handleCompleteDrag(interaction: InteractionType, path: Path<ScreenSpace>) = this
}
