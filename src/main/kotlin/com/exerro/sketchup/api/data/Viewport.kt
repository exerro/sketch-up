package com.exerro.sketchup.api.data

import com.exerro.sketchup.api.VectorSpaceTransform
import kotlin.math.pow

data class Viewport(
    val focus: SketchLocation,
    /** Size of the window being rendered to. */
    val windowSize: Vector<ScreenSpace>
) {
    val yCorrection = Vector<ScreenSpace>(0.0, (windowSize.y - windowSize.x) / 2)

    val worldToScreen = object: VectorSpaceTransform<WorldSpace, ScreenSpace> {
        override fun transformScalar(scalar: Scalar<WorldSpace>): Scalar<ScreenSpace> =
            scalar.map { it * 2.0.pow(focus.scale) * windowSize.x } .toSpace()

        override fun transformVector(vector: Vector<WorldSpace>): Vector<ScreenSpace> {
            val view = ((vector - focus.centre) * 2.0.pow(focus.scale)).toSpace<ViewSpace>()
            return yCorrection + (view + Vector(0.5)).toSpace<ScreenSpace>() * windowSize.x
        }
    }

    val screenToWorld = object: VectorSpaceTransform<ScreenSpace, WorldSpace> {
        override fun transformScalar(scalar: Scalar<ScreenSpace>): Scalar<WorldSpace> =
            scalar.map { it * 2.0.pow(-focus.scale) / windowSize.x } .toSpace()

        override fun transformVector(vector: Vector<ScreenSpace>): Vector<WorldSpace> {
            val view = ((vector - yCorrection) / windowSize.x - Vector(0.5)).toSpace<ViewSpace>()
            return focus.centre + (view * 2.0.pow(-focus.scale)).toSpace()
        }
    }

    fun toBoundingArea(): BoundingArea<WorldSpace> {
        val aspectRatioInv = windowSize.y / windowSize.x
        val halfWidth = 2.0.pow(-focus.scale)
        val halfHeight = halfWidth * aspectRatioInv

        return BoundingArea(
            focus.centre.x - halfWidth, focus.centre.x + halfWidth,
            focus.centre.y - halfHeight, focus.centre.y + halfHeight,
        )
    }
}