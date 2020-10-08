package com.exerro.sketchup.api.data

import com.exerro.sketchup.api.VectorSpaceTransform

/** Scalar value in the given vector space. */
inline class Scalar<Space: VectorSpace>(val value: Double) {
    fun <Space: VectorSpace> toSpace() = Scalar<Space>(value)
    inline fun map(fn: (Double) -> Double) = Scalar<Space>(fn(value))
    fun <OutSpace: VectorSpace> transform(transform: VectorSpaceTransform<Space, OutSpace>) = transform.transformScalar(this)

    operator fun plus(other: Scalar<Space>) = Scalar<Space>(value + other.value)
    operator fun minus(other: Scalar<Space>) = Scalar<Space>(value - other.value)
    operator fun times(other: Scalar<Space>) = Scalar<Space>(value * other.value)
    operator fun div(other: Scalar<Space>) = Scalar<Space>(value / other.value)
    operator fun plus(other: Double) = Scalar<Space>(value + other)
    operator fun minus(other: Double) = Scalar<Space>(value - other)
    operator fun times(other: Double) = Scalar<Space>(value * other)
    operator fun div(other: Double) = Scalar<Space>(value / other)
    operator fun unaryMinus() = Scalar<Space>(-value)
}

infix operator fun <Space: VectorSpace> Double.plus(other: Scalar<Space>) = Scalar<Space>(this + other.value)
infix operator fun <Space: VectorSpace> Double.minus(other: Scalar<Space>) = Scalar<Space>(this - other.value)
infix operator fun <Space: VectorSpace> Double.times(other: Scalar<Space>) = Scalar<Space>(this * other.value)
infix operator fun <Space: VectorSpace> Double.div(other: Scalar<Space>) = Scalar<Space>(this / other.value)
