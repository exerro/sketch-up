package com.exerro.sketchup.api.util

import com.exerro.sketchup.api.data.Scalar
import com.exerro.sketchup.api.data.VectorSpace

infix operator fun <Space: VectorSpace> Double.plus(other: Scalar<Space>) = Scalar<Space>(this + other.value)
infix operator fun <Space: VectorSpace> Double.minus(other: Scalar<Space>) = Scalar<Space>(this - other.value)
infix operator fun <Space: VectorSpace> Double.times(other: Scalar<Space>) = Scalar<Space>(this * other.value)
infix operator fun <Space: VectorSpace> Double.div(other: Scalar<Space>) = Scalar<Space>(this / other.value)
