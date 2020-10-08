package com.exerro.sketchup.api.data

import com.exerro.sketchup.api.VectorSpaceTransform

data class PathPoint<Space: VectorSpace>(
    val position: Vector<Space>,
    val size: Scalar<Space>,
    val t: Float
) {
    fun <OutSpace: VectorSpace> transform(transform: VectorSpaceTransform<Space, OutSpace>) =
        PathPoint(transform.transformVector(position), transform.transformScalar(size), t)

    companion object {
        fun <Space: VectorSpace> none() = PathPoint<Space>(Vector.origin(), Scalar(0.0), -1f)
    }
}
