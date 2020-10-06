package com.exerro.sketchup.data

import kotlin.math.max
import kotlin.math.min

/** Represents the bounds of a region in a given vector space. */
data class BoundingArea<Space: VectorSpace>(
    /** Minimum X value. */
    val xMin: Double,
    /** Maximum X value. */
    val xMax: Double,
    /** Minimum Y value. */
    val yMin: Double,
    /** Maximum Y value. */
    val yMax: Double,
) {
    /** Return true if this bounding area overlaps the [other] one. */
    infix fun overlaps(other: BoundingArea<Space>) =
        xMax > other.xMin && other.xMax > xMin &&
                yMax > other.yMin && other.yMax > yMin

    fun <OutSpace: VectorSpace> transform(transform: VectorSpaceTransform<Space, OutSpace>): BoundingArea<OutSpace> {
        val (xMin, yMin) = Vector<Space>(xMin, yMin).transform(transform)
        val (xMax, yMax) = Vector<Space>(xMax, yMax).transform(transform)
        return BoundingArea(xMin, xMax, yMin, yMax)
    }

    /** Central point of the bounding area. */
    val centre get() = Vector<Space>((xMin + xMax) / 2, (yMin + yMax) / 2)
    val size get() = Vector<Space>(xMax - xMin, yMax - yMin)
    val width = xMax - xMin
    val height = yMax - yMin
    /** Maximum of the [width] and [height]. */
    val maxSize = max(width, height)
    /** Minimum of the [width] and [height]. */
    val minSize = min(width, height)
}
