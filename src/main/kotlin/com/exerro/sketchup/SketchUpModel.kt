package com.exerro.sketchup

import com.exerro.sketchup.data.*
import com.exerro.sketchup.impl.BasicEntitySetImpl
import com.exerro.sketchup.impl.adapters.MainAdapter

data class SketchUpModel(
    val entities: EntitySet,
    val selectedEntities: Set<Entity>,
    val lastAddedEntity: Entity?,
    val viewport: Viewport,
    val adapter: Adapter,
    val pointer: Vector<ScreenSpace>,
    val visualHint: VisualHint,
) {
    companion object {
        val new = SketchUpModel(
            BasicEntitySetImpl.empty,
            emptySet(),
            null,
            Viewport(WorldPosition(0.0, 0.0), 0.0, WindowSize(0.0, 0.0)),
            MainAdapter,
            Vector.origin(),
            VisualHint.None,
        )
    }
}

sealed class VisualHint {
    object None: VisualHint()
    data class PathHint(val path: Path<ScreenSpace>): VisualHint()
    data class SelectionHint(val bounds: BoundingArea<ScreenSpace>): VisualHint()
}

typealias SketchUpMessage = WindowEvent
