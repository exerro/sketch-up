package com.exerro.sketchup.application

internal data class SketchUpModel(
    val sketch: SketchModel,
    val client: ClientModel,
    val application: ApplicationModel,
)
