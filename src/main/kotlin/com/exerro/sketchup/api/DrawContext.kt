package com.exerro.sketchup.api

import com.exerro.sketchup.api.data.*

interface DrawContext {
    fun point(point: Point<ScreenSpace>, colour: Colour)
    fun path(path: Path<ScreenSpace>, colour: Colour)
    fun boxOutline(boundingArea: BoundingArea<ScreenSpace>, colour: Colour)
}
