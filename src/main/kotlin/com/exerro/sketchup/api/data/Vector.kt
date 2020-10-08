@file:Suppress("EXPERIMENTAL_API_USAGE", "EXPERIMENTAL_UNSIGNED_LITERALS")

package com.exerro.sketchup.api.data

import com.exerro.sketchup.api.VectorSpaceTransform
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/** 2D vector in the given vector space. */
data class Vector<Space: VectorSpace>(
    val x: Double,
    val y: Double = x,
) {
    val magnitude2 get() = x * x + y * y
    val magnitude get() = sqrt(x * x + y * y)
    val normalised get() = this / magnitude

    fun rotate(theta: Double): Vector<Space> {
        val s = sin(theta)
        val c = cos(theta)
        return Vector(x * c - y * s, x * s + y * c)
    }

    fun <Space: VectorSpace> toSpace() = Vector<Space>(x, y)
    fun <OutSpace: VectorSpace> transform(transform: VectorSpaceTransform<Space, OutSpace>) = transform.transformVector(this)
    fun <OutSpace: VectorSpace> transformS(transform: VectorSpaceTransform<Space, OutSpace>) = transform.transformVectorS(this)

    operator fun plus(other: Vector<Space>) = Vector<Space>(x + other.x, y + other.y)
    operator fun minus(other: Vector<Space>) = Vector<Space>(x - other.x, y - other.y)
    operator fun times(other: Vector<Space>) = Vector<Space>(x * other.x, y * other.y)
    operator fun div(other: Vector<Space>) = Vector<Space>(x / other.x, y / other.y)
    operator fun times(scale: Scalar<Space>) = Vector<Space>(x * scale.value, y * scale.value)
    operator fun div(scale: Scalar<Space>) = Vector<Space>(x / scale.value, y / scale.value)
    operator fun times(scale: Double) = Vector<Space>(x * scale, y * scale)
    operator fun div(scale: Double) = Vector<Space>(x / scale, y / scale)
    operator fun unaryMinus() = Vector<Space>(-x, -y)

    companion object {
        fun <Space : VectorSpace> origin() = Vector<Space>(0.0, 0.0)
        fun <Space : VectorSpace> zero() = Vector<Space>(0.0, 0.0)
        fun <Space : VectorSpace> one() = Vector<Space>(1.0, 1.0)
    }
}
