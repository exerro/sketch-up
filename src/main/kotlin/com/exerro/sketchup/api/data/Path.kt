package com.exerro.sketchup.api.data

import com.exerro.sketchup.api.VectorSpaceTransform

data class Path<Space: VectorSpace>(
    val points: List<PathPoint<Space>>,
    val length: Scalar<Space>,
) {
    val firstOrNull: PathPoint<Space>? get() = points.firstOrNull()
    val lastOrNull: PathPoint<Space>? get() = points.lastOrNull()
    val startPoint: PathPoint<Space> get() = points.firstOrNull() ?: PathPoint.none()
    val endPoint: PathPoint<Space> get() = points.lastOrNull() ?: PathPoint.none()
    val offset: Vector<Space> get() = endPoint.position - startPoint.position

    fun isEmpty() = points.isEmpty()

    fun <OutSpace: VectorSpace> transform(transform: VectorSpaceTransform<Space, OutSpace>) =
        Path(points.map { it.transform(transform) }, length.transform(transform))

    fun tail(): Path<Space> {
        val t0 = points[0].t
        val scale = 1f - t0
        return Path(points.drop(1).map { it.copy(t = (it.t - t0) / scale) }, length * scale.toDouble())
    }

    operator fun plus(other: Path<Space>): Path<Space> = when {
        points.isEmpty() -> other
        other.points.isEmpty() -> this
        points.last() == other.points.first() -> this + other.tail()
        else -> {
            val lengthAddition = (points.last().position - other.points.first().position).magnitude
            val newLength = length + other.length + lengthAddition
            val p0ts = (length / newLength).value.toFloat()
            val p1ts = (other.length / newLength).value.toFloat()
            val p1td = ((length + lengthAddition) / newLength).value.toFloat()
            val p0p = points.map { it.copy(t = it.t * p0ts) }
            val p1p = other.points.map { it.copy(t = it.t * p1ts + p1td) }

            Path(p0p + p1p, newLength)
        }
    }

    companion object {
        fun <Space: VectorSpace> empty() = Path<Space>(emptyList(), Scalar(0.0))

        fun <Space: VectorSpace> of(position: Vector<Space>, size: Scalar<Space>) =
            Path(listOf(PathPoint(position, size, 0f)), Scalar(0.0))

        fun <Space: VectorSpace> of(point: Point<Space>) =
            Path(listOf(PathPoint(point.position, point.size, 0f)), Scalar(0.0))

        fun <Space: VectorSpace> of(point: PathPoint<Space>) =
            Path(listOf(point), Scalar(0.0))
    }
}
