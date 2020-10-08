package com.exerro.sketchup.impl.entities

import com.exerro.sketchup.api.data.Viewport
import com.exerro.sketchup.api.DrawContext
import com.exerro.sketchup.api.Entity
import com.exerro.sketchup.api.data.BoundingArea
import com.exerro.sketchup.api.data.Colour
import com.exerro.sketchup.api.data.Point
import com.exerro.sketchup.api.data.WorldSpace

data class PointEntity(
    val point: Point<WorldSpace>,
    val colour: Colour,
): Entity {
    override val boundingArea = BoundingArea<WorldSpace>(
        point.position.x - point.size.value,
        point.position.x + point.size.value,
        point.position.y - point.size.value,
        point.position.y + point.size.value,
    )

    override val detail = point.size

    override fun DrawContext.draw(viewport: Viewport) {
        point(point.transform(viewport.worldToScreen), colour)
    }
}
