package com.exerro.sketchup.util

import com.exerro.sketchup.SketchUpModel
import com.exerro.sketchup.VisualHint
import com.exerro.sketchup.data.Colour
import com.exerro.sketchup.impl.NanoVGRenderer

fun NanoVGRenderer.drawModel(model: SketchUpModel) = draw(
    model.viewport.windowSize.x.toInt(),
    model.viewport.windowSize.y.toInt()
) {
    model.entities.allVisible(model.viewport).forEach {
        it.run { draw(model.viewport) }
    }

    model.selectedEntities.forEach {
        boxOutline(it.boundingArea.transform(model.viewport.worldToScreen), Colour.blue.lighten(0.3f))
    }

    when (model.visualHint) {
        is VisualHint.PathHint -> path(model.visualHint.path, Colour.lighterGrey)
        is VisualHint.SelectionHint -> boxOutline(model.visualHint.bounds, Colour.white)
        is VisualHint.None -> {}
    }
}
