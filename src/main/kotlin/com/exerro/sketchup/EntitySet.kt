package com.exerro.sketchup

interface EntitySet {
    fun add(entity: Entity): EntitySet
    fun all(): Set<Entity>
    fun allVisible(worldBoundingArea: BoundingArea<WorldSpace>, pixelsPerUnit: Double): Iterable<Entity>
}

class BasicEntitySetImpl private constructor(
    private val entities: Set<Entity>
): EntitySet {
    override fun add(entity: Entity) = BasicEntitySetImpl(entities + entity)
    override fun all() = entities
    override fun allVisible(worldBoundingArea: BoundingArea<WorldSpace>, pixelsPerUnit: Double) = entities
        .filter { it.detail.value > worldBoundingArea.maxSize / pixelsPerUnit * DETAIL_THRESHOLD }
        .filter { it.boundingArea overlaps worldBoundingArea }

    companion object {
        val empty = BasicEntitySetImpl(emptySet())
        const val DETAIL_THRESHOLD = 0.5
    }
}
