package com.exerro.sketchup

import kotlin.math.pow

data class Viewport(
    /** Centre of the viewport. */
    val centre: WorldPosition,
    /** The width of the viewport in world-units is 2^[scale]. */
    val scale: Double,
    /** Size of the window being rendered to. */
    val windowSize: WindowSize
) {
    /** Transform a position from world space into screen space. */
    fun transform(position: WorldPosition): WindowPosition {
        val view = ((position - centre) * 2.0.pow(scale)).toSpace<ViewSpace>()
        return Vector.origin<ScreenSpace>() + (view + Vector(0.5)).toSpace<ScreenSpace>() * windowSize
    }

    /** Transform a position from screen space into world space. */
    fun inverseTransform(position: WindowPosition): WorldPosition {
        val view = (position / windowSize - Vector(0.5)).toSpace<ViewSpace>()
        return centre + (view * 2.0.pow(-scale)).toSpace()
    }

    /** Transform a position from world space into screen space. */
    fun transform(scalar: Scalar<WorldSpace>): Scalar<ScreenSpace> =
        (scalar * 2.0.pow(scale)).toSpace()

    /** Transform a position from screen space into world space. */
    fun inverseTransform(scalar: Scalar<ScreenSpace>): Scalar<WorldSpace> =
        (scalar * 2.0.pow(-scale)).toSpace()

    /** Transform a position delta from screen space into world space. */
    fun inverseTransformSize(delta: Vector<ScreenSpace>): Vector<WorldSpace> =
        (delta / windowSize * 2.0.pow(-scale)).toSpace()

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