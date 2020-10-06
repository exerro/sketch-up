package com.exerro.sketchup.data

inline class Path<Space: VectorSpace>(
    val points: List<Point<Space>>
) {
    val firstOrNull: Point<Space>? get() = points.firstOrNull()
    val lastOrNull: Point<Space>? get() = points.lastOrNull()
    val first: Point<Space> get() = points.firstOrNull() ?: Point(Vector.origin(), Scalar(0.0))
    val last: Point<Space> get() = points.lastOrNull() ?: Point(Vector.origin(), Scalar(0.0))
    val offset: Vector<Space> get() = last.position - first.position

    fun <OutSpace: VectorSpace> transform(transform: VectorSpaceTransform<Space, OutSpace>) =
        Path(points.map { it.transform(transform) })

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

        fun <Space: VectorSpace> of(point: Point<Space>) =
            Path(listOf(point))
    }
}
