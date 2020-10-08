package com.exerro.sketchup.application

import com.exerro.sketchup.api.data.PointerMode
import com.exerro.sketchup.api.data.Path
import com.exerro.sketchup.api.data.Point
import com.exerro.sketchup.api.data.ScreenSpace

internal interface Adapter {
    fun SketchUpModel.handlePrimaryPointerPress(alternate: Boolean, point: Point<ScreenSpace>) = this
    fun SketchUpModel.handleSecondaryPointerPress(alternate: Boolean, point: Point<ScreenSpace>) = this
    fun SketchUpModel.handleTertiaryPointerPress(alternate: Boolean, point: Point<ScreenSpace>) = this
    fun SketchUpModel.completePrimaryPointerDrag(alternate: Boolean, path: Path<ScreenSpace>) = this
    fun SketchUpModel.completeSecondaryPointerDrag(alternate: Boolean, path: Path<ScreenSpace>) = this
    fun SketchUpModel.handlePointerDrag(mode: PointerMode, alternate: Boolean, path: Path<ScreenSpace>) = setPathHint(path)
}
