package com.exerro.sketchup

import com.exerro.sketchup.data.*
import com.exerro.sketchup.util.boundingArea

/** An entity in the world. */
interface Entity {
    /** The bounds of the entity (min/max on each axis). */
    val boundingArea: BoundingArea<WorldSpace>
    /** A parameter representing coarsest detail of the object, i.e. the
     *  maximum width of a line, used to trim entities that are too detailed
     *  from rendering. */
    val detail: Scalar<WorldSpace>
    /** Draw the entity to the screen. */
    fun DrawContext.draw(viewport: Viewport)
}

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

data class PathEntity(
    val path: Path<WorldSpace>,
    val colour: Colour,
): Entity {
    override val boundingArea = path.boundingArea
    override val detail = Scalar<WorldSpace>(path.points.map { it.size.value } .maxOrNull() ?: 0.0)

    override fun DrawContext.draw(viewport: Viewport) {
        val points = path.points.map { Point(it.position.transform(viewport.worldToScreen),
            it.size.transform(viewport.worldToScreen)
        ) }
        path(Path(points), colour)
    }
}
