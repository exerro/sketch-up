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
    val pointerMappings: Map<PointerMode, InteractionType>,
) {
    companion object {
        val new = ApplicationModel(
            selectedEntities = emptySet(),
            lastAddedEntity = null,
            adapter = MainAdapter,
            pointer = Vector.origin(),
            visualHint = VisualHint.none,
            viewport = Viewport(SketchLocation(Vector(0.0, 0.0), 0.0), Vector(0.0, 0.0)),
            pointerMappings = defaultPointerMappings
        )

        private val defaultPointerMappings get() = mapOf(
            PointerButton.Primary alternate false to InteractionType.Sketch,
            PointerButton.Primary alternate true to InteractionType.SelectMeta,
            PointerButton.Secondary alternate false to InteractionType.SelectSpatial,
            PointerButton.Secondary alternate true to InteractionType.SelectTemporal,
            PointerButton.Tertiary alternate false to InteractionType.Navigation,
            PointerButton.Tertiary alternate true to InteractionType.SpecialNavigation,
        )

        private infix fun PointerButton.alternate(alt: Boolean) =
            PointerMode(emptySet(), this, alt)
    }
}
