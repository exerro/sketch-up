package com.exerro.sketchup.application

import com.exerro.sketchup.api.EntitySet
import com.exerro.sketchup.api.SketchHost

internal data class SketchModel private constructor(
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
