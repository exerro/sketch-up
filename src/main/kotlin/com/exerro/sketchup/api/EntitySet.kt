package com.exerro.sketchup.api

import com.exerro.sketchup.api.data.Viewport

interface EntitySet {
    fun add(entity: Entity): EntitySet
    fun all(): Set<Entity>
    fun allVisible(viewport: Viewport): Iterable<Entity>
}
