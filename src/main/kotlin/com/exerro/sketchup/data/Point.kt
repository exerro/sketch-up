package com.exerro.sketchup.data

data class Point<Space: VectorSpace> (
    val position: Vector<Space>,
    val size: Scalar<Space>
) {
    fun <OutSpace: VectorSpace> transform(transform: VectorSpaceTransform<Space, OutSpace>) =
        Point(transform.transformVector(position), transform.transformScalar(size))
}
