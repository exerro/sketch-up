package com.exerro.sketchup.application

import com.exerro.sketchup.api.data.*
import com.exerro.sketchup.impl.entities.PathEntity
import com.exerro.sketchup.impl.entities.PointEntity
import com.exerro.sketchup.util.boundingArea
import kotlin.math.PI
import kotlin.math.pow
import kotlin.math.sin

internal object MainAdapter: Adapter {
    override fun SketchUpModel.handlePrimaryPointerPress(alternate: Boolean, point: Point<ScreenSpace>) = when (alternate) {
        true -> copy(application = application.copy(selectedEntities = setOfNotNull(application.lastAddedEntity)))
        else -> addPointEntity(newPoint(point, application.viewport))
    }

    override fun SketchUpModel.completePrimaryPointerDrag(alternate: Boolean, path: Path<ScreenSpace>) = when {
        alternate -> selectEntities(path)
        else -> addPathEntity(newPath(path, application.viewport))
    }

    override fun SketchUpModel.completeSecondaryPointerDrag(alternate: Boolean, path: Path<ScreenSpace>) = this

    override fun SketchUpModel.handlePointerDrag(mode: PointerMode, alternate: Boolean, path: Path<ScreenSpace>) = when {
        alternate -> setSelectionHint(path.startPoint, path.endPoint).selectEntities(path)
        else -> setPathHint(path)
    }
}

private fun SketchUpModel.addPointEntity(point: Point<WorldSpace>) =
    addEntity(PointEntity(point, client.colour))

private fun SketchUpModel.addPathEntity(path: Path<WorldSpace>) =
    addEntity(PathEntity(path, client.colour)).addEntity(PathEntity(path.approx(), client.colour))

private fun SketchUpModel.selectEntities(path: Path<ScreenSpace>): SketchUpModel {
    val selectedArea = (Path.of(path.startPoint) + Path.of(path.endPoint)).boundingArea.transform(application.viewport.screenToWorld)
    val selected = sketch.snapshot.entities.allVisible(application.viewport).filter { it.boundingArea in selectedArea }
    return copy(application = application.copy(selectedEntities = selected.toSet()))
}

private fun newPoint(point: Point<ScreenSpace>, viewport: Viewport) =
    Point(point.position, point.size * POINT_SIZE).transform(viewport.screenToWorld)

private fun newPath(path: Path<ScreenSpace>, viewport: Viewport) =
    Path(path.points.map { it.copy(size = it.size * LINE_WIDTH) }, path.length).transform(viewport.screenToWorld)

private fun <Space: VectorSpace> Path<Space>.sinWave() = Path(
    points.map { PathPoint(it.position, it.size * (1 + sin(it.t * PI)), it.t) },
    length
)

private fun <Space: VectorSpace> Path<Space>.approx(): Path<Space> {
    val ts = points.map { it.t.toDouble() }
    val xs = points.map { it.position.x }
    val ys = points.map { it.position.y }
    val ss = points.map { it.size.value }

    val xCoeffs = cubicApproximate(xs, ts)
    val yCoeffs = cubicApproximate(ys, ts)
    val sCoeffs = cubicApproximate(ss, ts)
    val samples = (0 .. 50).map { it / 50.0 }

    return samples.fold(Path.empty()) { path, t ->
        val x = applyPoly(xCoeffs, t) + 1
        val y = applyPoly(yCoeffs, t)
        val s = applyPoly(sCoeffs, t)
        path + Path.of(Vector(x, y), Scalar(s))
    }
}

private fun applyPoly(poly: DoubleArray, t: Double) =
    poly.mapIndexed { i, v -> v * t.pow(i.toDouble()) } .sum()

private fun cubicApproximate(ys: List<Double>, xs: List<Double>): DoubleArray {
    val X = Matrix.mapped(ys.size, 4) { row, column ->
        xs[row].pow(column.toDouble())
    }
    val XT = X.transpose()
    val y = Matrix.mapped(ys.size, 1) { c, _ -> ys[c] }
    return ((XT * X).inverse() * XT * y).values
}

private const val POINT_SIZE = 5.0
private const val LINE_WIDTH = 2.0
