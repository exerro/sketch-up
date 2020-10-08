package com.exerro.sketchup.api

import com.exerro.sketchup.api.data.Scalar
import com.exerro.sketchup.api.data.Vector
import com.exerro.sketchup.api.data.VectorSpace

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
