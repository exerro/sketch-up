package com.exerro.sketchup.api

import com.exerro.sketchup.api.data.*

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
