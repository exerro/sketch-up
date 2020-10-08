package com.exerro.sketchup.util

import com.exerro.sketchup.api.data.BoundingArea
import com.exerro.sketchup.api.data.Path
import com.exerro.sketchup.api.data.VectorSpace

val <Space: VectorSpace> Path<Space>.boundingArea get() = BoundingArea<Space>(
    points.map { it.position.x - it.size.value } .minOrNull() ?: 0.0,
    points.map { it.position.x + it.size.value } .maxOrNull() ?: 0.0,
    points.map { it.position.y - it.size.value } .minOrNull() ?: 0.0,
    points.map { it.position.y + it.size.value } .maxOrNull() ?: 0.0,
)