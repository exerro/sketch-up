package com.exerro.sketchup.model

import com.exerro.sketchup.Adapter
import com.exerro.sketchup.Entity
import com.exerro.sketchup.Viewport
import com.exerro.sketchup.VisualHint
import com.exerro.sketchup.data.ScreenSpace
import com.exerro.sketchup.data.Vector
import com.exerro.sketchup.data.WindowSize
import com.exerro.sketchup.data.WorldPosition
import com.exerro.sketchup.impl.adapters.MainAdapter

data class ApplicationModel(
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
            viewport = Viewport(SketchFocus(WorldPosition(0.0, 0.0), 0.0), WindowSize(0.0, 0.0)),
        )
    }
}
