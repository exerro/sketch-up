package com.exerro.sketchup.application

import com.exerro.sketchup.api.data.Colour
import com.exerro.sketchup.impl.NanoVGRenderer

internal fun NanoVGRenderer.drawModel(model: SketchUpModel) = draw(
    model.application.viewport.windowSize.x.toInt(),
    model.application.viewport.windowSize.y.toInt()
) {
    model.sketch.snapshot.entities.allVisible(model.application.viewport).forEach {
        it.run { draw(model.application.viewport) }
    }

    model.application.selectedEntities.forEach {
        boxOutline(it.boundingArea.transform(model.application.viewport.worldToScreen), Colour.blue.lighten(0.3f))
    }

    model.application.visualHint.run { draw() }
}