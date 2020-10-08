package com.exerro.sketchup.impl.entities

import com.exerro.sketchup.api.data.Viewport
import com.exerro.sketchup.api.DrawContext
import com.exerro.sketchup.api.Entity
import com.exerro.sketchup.api.data.Colour
import com.exerro.sketchup.api.data.Path
import com.exerro.sketchup.api.data.Scalar
import com.exerro.sketchup.api.data.WorldSpace
import com.exerro.sketchup.util.boundingArea

data class PathEntity(
    val path: Path<WorldSpace>,
    val colour: Colour,
): Entity {
    override val boundingArea = path.boundingArea
    override val detail = Scalar<WorldSpace>(path.points.map { it.size.value }.maxOrNull() ?: 0.0)

    override fun DrawContext.draw(viewport: Viewport) {
        path(path.transform(viewport.worldToScreen), colour)
    }
}
