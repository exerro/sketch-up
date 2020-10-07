package com.exerro.sketchup.impl

import com.exerro.sketchup.Entity
import com.exerro.sketchup.EntitySet
import com.exerro.sketchup.Viewport
import com.exerro.sketchup.data.BoundingArea
import com.exerro.sketchup.data.WorldSpace

class BasicEntitySetImpl private constructor(
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
