package com.exerro.sketchup.application

import com.exerro.sketchup.api.Entity
import com.exerro.sketchup.api.VisualHint
import com.exerro.sketchup.api.data.*
import com.exerro.sketchup.api.util.none

internal data class ApplicationModel(
    val selectedEntities: Set<Entity>,
    val lastAddedEntity: Entity?,
    val adapter: Adapter,
    val pointer: Vector<ScreenSpace>,
    val visualHint: VisualHint,
    val viewport: Viewport,
) {
    companion object {
        val new = ApplicationModel(
            selectedEntities = emptySet(),
            lastAddedEntity = null,
            adapter = MainAdapter,
            pointer = Vector.origin(),
            visualHint = VisualHint.none,
            viewport = Viewport(SketchLocation(Vector(0.0, 0.0), 0.0), Vector(0.0, 0.0)),
        )
    }
}
