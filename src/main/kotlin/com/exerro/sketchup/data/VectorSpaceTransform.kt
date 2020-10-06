package com.exerro.sketchup.data

interface VectorSpaceTransform<In: VectorSpace, Out: VectorSpace> {
    fun transformScalar(scalar: Scalar<In>): Scalar<Out>
    fun transformVector(vector: Vector<In>): Vector<Out> = Vector(
        transformScalar(Scalar(vector.x)).value,
        transformScalar(Scalar(vector.y)).value,
    )
    fun transformVectorS(vector: Vector<In>): Vector<Out> = Vector(
        transformScalar(Scalar(vector.x)).value,
        transformScalar(Scalar(vector.y)).value,
    )
}
