package com.exerro.sketchup.api

import com.exerro.sketchup.api.DrawContext
import com.exerro.sketchup.api.data.BoundingArea
import com.exerro.sketchup.api.data.Colour
import com.exerro.sketchup.api.data.Path
import com.exerro.sketchup.api.data.ScreenSpace

fun interface VisualHint {
    fun DrawContext.draw()

    companion object
}
