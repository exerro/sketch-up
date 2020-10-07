package com.exerro.sketchup.model

import com.exerro.sketchup.EntitySet
import com.exerro.sketchup.SketchHost

data class SketchModel private constructor(
    val host: SketchHost,
    val snapshot: SketchSnapshot
) {
    companion object {
        fun fromHost(host: SketchHost) = SketchModel(
            host = host,
            snapshot = host.constantSnapshot()
        )
    }
}

data class SketchSnapshot(
    val entities: EntitySet,
)
