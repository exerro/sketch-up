package com.exerro.sketchup.impl

import com.exerro.sketchup.api.Entity
import com.exerro.sketchup.api.EntitySet
import com.exerro.sketchup.api.data.Viewport

internal class BasicEntitySetImpl private constructor(
    private val entities: Set<Entity>
): EntitySet {
    override fun add(entity: Entity) = BasicEntitySetImpl(entities + entity)
    override fun all() = entities
    override fun allVisible(viewport: Viewport) = entities
        .filter { it.detail.transform(viewport.worldToScreen).value > DETAIL_THRESHOLD }
        .filter { it.boundingArea overlaps viewport.toBoundingArea() }

    companion object {
        val empty = BasicEntitySetImpl(emptySet())
        const val DETAIL_THRESHOLD = 0.2
    }
}
