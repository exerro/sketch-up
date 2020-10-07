package com.exerro.sketchup

import com.exerro.sketchup.model.ApplicationModel
import com.exerro.sketchup.model.ClientModel
import com.exerro.sketchup.model.SketchModel

data class SketchUpModel(
    val sketch: SketchModel,
    val client: ClientModel,
    val application: ApplicationModel,
) {
    companion object {
        val new = SketchUpModel(
            SketchModel.fromHost(LocalDebugSketchHost),
            ClientModel(),
            ApplicationModel.new,
        )
    }
}

typealias SketchUpMessage = WindowEvent
