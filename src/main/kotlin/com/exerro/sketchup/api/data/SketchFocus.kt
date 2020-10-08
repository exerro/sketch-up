package com.exerro.sketchup.api.data

data class SketchFocus(
    /** Position in the world considered to be the centre of this view. */
    val centre: Vector<WorldSpace>,
    /** 1 unit in world-space is 2^[scale] units in view space. */
    val scale: Double,
)
