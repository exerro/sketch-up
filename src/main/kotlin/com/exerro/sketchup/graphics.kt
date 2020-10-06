package com.exerro.sketchup

inline class Path<Space: VectorSpace>(
    val points: List<Point<Space>>
) {
    operator fun plus(other: Path<Space>) = when {
        points.isEmpty() -> other
        other.points.isEmpty() -> this
        points.last() == other.points.first() -> Path(points + other.points.drop(1))
        else -> Path(points + other.points)
    }

    companion object {
        fun <Space: VectorSpace> empty() = Path<Space>(emptyList())

        fun <Space: VectorSpace> of(position: Vector<Space>, size: Scalar<Space>) =
            Path(listOf(Point(position, size)))
    }
}

data class Point<Space: VectorSpace> (
    val position: Vector<Space>,
    val size: Scalar<Space>
)
