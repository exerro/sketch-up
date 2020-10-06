package com.exerro.sketchup

val PointerDrag.totalMovement get() = lastPosition - firstPosition

fun Viewport.transform(path: Path<WorldSpace>): Path<ScreenSpace> =
    Path(path.points.map { Point(transform(it.position), transform(it.size)) })

fun Viewport.inverseTransform(path: Path<ScreenSpace>): Path<WorldSpace> =
    Path(path.points.map { Point(inverseTransform(it.position), inverseTransform(it.size)) })
