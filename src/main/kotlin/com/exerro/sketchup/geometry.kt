@file:Suppress("EXPERIMENTAL_API_USAGE", "EXPERIMENTAL_UNSIGNED_LITERALS")

package com.exerro.sketchup

import kotlin.math.max
import kotlin.math.min

typealias WindowPosition = Vector<ScreenSpace>
typealias WorldPosition = Vector<WorldSpace>
typealias WindowSize = Vector<ScreenSpace>

sealed class VectorSpace
object ScreenSpace: VectorSpace()
object ViewSpace: VectorSpace()
object WorldSpace: VectorSpace()

inline class Scalar<Space: VectorSpace>(val value: Double)

data class Vector<Space: VectorSpace>(
    val x: Double,
    val y: Double = x,
) {
    companion object {
        fun <Space : VectorSpace> origin() = Vector<Space>(0.0, 0.0)
        fun <Space : VectorSpace> zero() = Vector<Space>(0.0, 0.0)
        fun <Space : VectorSpace> one() = Vector<Space>(1.0, 1.0)
    }
}

/** Represents the bounds of a region. */
data class BoundingArea<Space: VectorSpace>(
    val xMin: Double,
    val xMax: Double,
    val yMin: Double,
    val yMax: Double,
) {
    infix fun overlaps(other: BoundingArea<Space>) =
        xMax > other.xMin && other.xMax > xMin &&
                yMax > other.yMin && other.yMax > yMin

    val centre get() = Vector<Space>((xMin + xMax) / 2, (yMin + yMax) / 2)
    val size get() = Vector<Space>(xMax - xMin, yMax - yMin)
    val width = xMax - xMin
    val height = yMax - yMin
    val maxSize = max(width, height)
    val minSize = min(width, height)
}
////////////////////////////////////////////////////////////////////////////////

fun <Space: VectorSpace> Scalar<*>.toSpace() = Scalar<Space>(value)
fun <Space: VectorSpace> Vector<*>.toSpace() = Vector<Space>(x, y)

infix operator fun <Space: VectorSpace> Vector<Space>.plus(other: Vector<Space>) = Vector<Space>(x + other.x, y + other.y)
infix operator fun <Space: VectorSpace> Vector<Space>.minus(other: Vector<Space>) = Vector<Space>(x - other.x, y - other.y)
infix operator fun <Space: VectorSpace> Vector<Space>.times(other: Vector<Space>) = Vector<Space>(x * other.x, y * other.y)
infix operator fun <Space: VectorSpace> Vector<Space>.div(other: Vector<Space>) = Vector<Space>(x / other.x, y / other.y)
infix operator fun <Space: VectorSpace> Vector<Space>.times(scale: Scalar<Space>) = Vector<Space>(x * scale.value, y * scale.value)
infix operator fun <Space: VectorSpace> Vector<Space>.div(scale: Scalar<Space>) = Vector<Space>(x / scale.value, y / scale.value)
infix operator fun <Space: VectorSpace> Vector<Space>.times(scale: Double) = Vector<Space>(x * scale, y * scale)
infix operator fun <Space: VectorSpace> Vector<Space>.div(scale: Double) = Vector<Space>(x / scale, y / scale)

infix operator fun <Space: VectorSpace> Scalar<Space>.plus(other: Scalar<Space>) = Scalar<Space>(value + other.value)
infix operator fun <Space: VectorSpace> Scalar<Space>.minus(other: Scalar<Space>) = Scalar<Space>(value - other.value)
infix operator fun <Space: VectorSpace> Scalar<Space>.times(other: Scalar<Space>) = Scalar<Space>(value * other.value)
infix operator fun <Space: VectorSpace> Scalar<Space>.div(other: Scalar<Space>) = Scalar<Space>(value / other.value)
infix operator fun <Space: VectorSpace> Scalar<Space>.plus(other: Double) = Scalar<Space>(value + other)
infix operator fun <Space: VectorSpace> Scalar<Space>.minus(other: Double) = Scalar<Space>(value - other)
infix operator fun <Space: VectorSpace> Scalar<Space>.times(other: Double) = Scalar<Space>(value * other)
infix operator fun <Space: VectorSpace> Scalar<Space>.div(other: Double) = Scalar<Space>(value / other)
