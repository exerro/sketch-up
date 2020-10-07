package com.exerro.sketchup

import com.exerro.sketchup.data.Path
import com.exerro.sketchup.data.Point
import com.exerro.sketchup.data.ScreenSpace
import com.exerro.sketchup.util.setPathHint

interface Adapter {
    fun SketchUpModel.handlePrimaryPointerPress(alternate: Boolean, point: Point<ScreenSpace>) = this
    fun SketchUpModel.handleSecondaryPointerPress(alternate: Boolean, point: Point<ScreenSpace>) = this
    fun SketchUpModel.handleTertiaryPointerPress(alternate: Boolean, point: Point<ScreenSpace>) = this
    fun SketchUpModel.completePrimaryPointerDrag(alternate: Boolean, path: Path<ScreenSpace>) = this
    fun SketchUpModel.completeSecondaryPointerDrag(alternate: Boolean, path: Path<ScreenSpace>) = this
    fun SketchUpModel.handlePointerDrag(mode: PointerMode, alternate: Boolean, path: Path<ScreenSpace>) = setPathHint(path)
}
