package com.exerro.sketchup

interface EntitySet {
    fun add(entity: Entity): EntitySet
    fun all(): Set<Entity>
    fun allVisible(viewport: Viewport): Iterable<Entity>
}
