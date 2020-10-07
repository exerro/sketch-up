package com.exerro.sketchup.impl.adapters

import com.exerro.sketchup.*
import com.exerro.sketchup.data.*
import com.exerro.sketchup.util.*
import kotlin.math.PI
import kotlin.math.pow
import kotlin.math.sin

object MainAdapter: Adapter {
    override fun SketchUpModel.handlePrimaryPointerPress(alternate: Boolean, point: Point<ScreenSpace>) = when (alternate) {
        true -> copy(application = application.copy(selectedEntities = setOfNotNull(application.lastAddedEntity)))
        else -> addPointEntity(newPoint(point, application.viewport), Colour.blue)
    }

    override fun SketchUpModel.completePrimaryPointerDrag(alternate: Boolean, path: Path<ScreenSpace>) = when {
        alternate -> selectEntities(path)
        else -> addPathEntity(newPath(path, application.viewport), Colour.blue)
    }

    override fun SketchUpModel.completeSecondaryPointerDrag(alternate: Boolean, path: Path<ScreenSpace>) = this

    override fun SketchUpModel.handlePointerDrag(mode: PointerMode, alternate: Boolean, path: Path<ScreenSpace>) = when {
        alternate -> setSelectionHint(path.first, path.last).selectEntities(path)
        else -> setPathHint(path)
    }
}

private fun SketchUpModel.addPointEntity(point: Point<WorldSpace>, colour: Colour) =
    addEntity(PointEntity(point, colour))

private fun SketchUpModel.addPathEntity(path: Path<WorldSpace>, colour: Colour) =
    addEntity(PathEntity(path.sinWave(), colour)).addEntity(PathEntity(path.approx(), colour))

private fun SketchUpModel.selectEntities(path: Path<ScreenSpace>): SketchUpModel {
    val selectedArea = (Path.of(path.first) + Path.of(path.last)).boundingArea.transform(application.viewport.screenToWorld)
    val selected = sketch.snapshot.entities.allVisible(application.viewport).filter { it.boundingArea in selectedArea }
    return copy(application = application.copy(selectedEntities = selected.toSet()))
}

private fun newPoint(point: Point<ScreenSpace>, viewport: Viewport) =
    Point(point.position, point.size * POINT_SIZE).transform(viewport.screenToWorld)

private fun newPath(path: Path<ScreenSpace>, viewport: Viewport) =
    Path(path.points.map { it.copy(size = it.size * LINE_WIDTH) }, path.length).transform(viewport.screenToWorld)

private fun <Space: VectorSpace> Path<Space>.sinWave() = Path(
    points.map { PathPoint(it.position, it.size * (1 + 10 * sin(it.t * PI)), it.t) },
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
    val samples = (0 .. 10).map { it / 10.0 }

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

const val POINT_SIZE = 5.0
const val LINE_WIDTH = 2.0
