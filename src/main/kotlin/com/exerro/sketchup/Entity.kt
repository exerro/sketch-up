package com.exerro.sketchup

/** An entity in the world. */
interface Entity {
    /** The bounds of the entity (min/max on each axis). */
    val boundingArea: BoundingArea<WorldSpace>
    /** A parameter representing coarsest detail of the object, i.e. the
     *  maximum width of a line, used to trim entities that are too detailed
     *  from rendering. */
    val detail: Scalar<WorldSpace>
    /** Draw the entity to the screen. */
    fun RendererDrawContext.draw(viewport: Viewport)
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

    override fun RendererDrawContext.draw(viewport: Viewport) {
        point(viewport.transform(point.position), viewport.transform(point.size).value.toFloat(), colour)
    }
}

data class PathEntity(
    val path: Path<WorldSpace>,
    val colour: Colour,
): Entity {
    override val boundingArea = BoundingArea<WorldSpace>(
        path.points.map { it.position.x - it.size.value } .minOrNull() ?: 0.0,
        path.points.map { it.position.x + it.size.value } .maxOrNull() ?: 0.0,
        path.points.map { it.position.y - it.size.value } .minOrNull() ?: 0.0,
        path.points.map { it.position.y + it.size.value } .maxOrNull() ?: 0.0,
    )

    override val detail = Scalar<WorldSpace>(path.points.map { it.size.value } .maxOrNull() ?: 0.0)

    override fun RendererDrawContext.draw(viewport: Viewport) {
        val points = path.points.map { Point(viewport.transform(it.position), viewport.transform(it.size)) }
        ipath(Path(points), colour)
    }
}
