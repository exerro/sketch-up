package com.exerro.sketchup.model

import com.exerro.sketchup.data.Scalar
import com.exerro.sketchup.data.Vector
import com.exerro.sketchup.data.WorldSpace

class ClientModel(

)

data class SketchFocus(
    /** Position in the world considered to be the centre of this view. */
    val centre: Vector<WorldSpace>,
    /** 1 unit in world-space is 2^[scale] units in view space. */
    val scale: Double,
)
