package com.exerro.sketchup

import com.exerro.sketchup.data.*
import kotlin.math.pow

data class Viewport(
    /** Centre of the viewport. */
    val centre: Vector<WorldSpace>,
    /** The width of the viewport in world-units is 2^[scale]. */
    val scale: Double,
    /** Size of the window being rendered to. */
    val windowSize: WindowSize
) {
    val yCorrection = Vector<ScreenSpace>(0.0, (windowSize.y - windowSize.x) / 2)

    val worldToScreen = object: VectorSpaceTransform<WorldSpace, ScreenSpace> {
        override fun transformScalar(scalar: Scalar<WorldSpace>): Scalar<ScreenSpace> =
            scalar.map { it * 2.0.pow(scale) * windowSize.x } .toSpace()

        override fun transformVector(vector: Vector<WorldSpace>): Vector<ScreenSpace> {
            val view = ((vector - centre) * 2.0.pow(scale)).toSpace<ViewSpace>()
            return yCorrection + (view + Vector(0.5)).toSpace<ScreenSpace>() * windowSize.x
        }
    }

    val screenToWorld = object: VectorSpaceTransform<ScreenSpace, WorldSpace> {
        override fun transformScalar(scalar: Scalar<ScreenSpace>): Scalar<WorldSpace> =
            scalar.map { it * 2.0.pow(-scale) / windowSize.x } .toSpace()

        override fun transformVector(vector: Vector<ScreenSpace>): Vector<WorldSpace> {
            val view = ((vector - yCorrection) / windowSize.x - Vector(0.5)).toSpace<ViewSpace>()
            return centre + (view * 2.0.pow(-scale)).toSpace()
        }
    }

    fun toBoundingArea(): BoundingArea<WorldSpace> {
        val aspectRatioInv = windowSize.y / windowSize.x
        val halfWidth = 2.0.pow(-scale)
        val halfHeight = halfWidth * aspectRatioInv

        return BoundingArea(
            centre.x - halfWidth, centre.x + halfWidth,
            centre.y - halfHeight, centre.y + halfHeight,
        )
    }
}