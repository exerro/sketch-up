package com.exerro.sketchup.api.data

import com.exerro.sketchup.api.VectorSpaceTransform

data class Point<Space: VectorSpace> (
    val position: Vector<Space>,
    val size: Scalar<Space>
) {
    fun <OutSpace: VectorSpace> transform(transform: VectorSpaceTransform<Space, OutSpace>) =
        Point(transform.transformVector(position), transform.transformScalar(size))
}
