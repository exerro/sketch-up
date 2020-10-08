package com.exerro.sketchup.application

import com.exerro.sketchup.api.Entity
import com.exerro.sketchup.api.VisualHint
import com.exerro.sketchup.api.data.*
import com.exerro.sketchup.api.util.path
import com.exerro.sketchup.api.util.rectangularSelection
import com.exerro.sketchup.application.MainAdapter.handleCompleteDrag
import com.exerro.sketchup.impl.entities.PathEntity
import com.exerro.sketchup.impl.entities.PointEntity
import com.exerro.sketchup.util.boundingArea
import kotlin.math.PI
import kotlin.math.pow
import kotlin.math.sin

internal object MainAdapter: Adapter {
    override fun SketchUpModel.handlePress(interaction: InteractionType, point: Point<ScreenSpace>) = when (interaction) {
        InteractionType.Sketch -> addPointEntity(newPoint(point, application.viewport))
        InteractionType.SelectMeta -> copy(application = application.copy(selectedEntities = setOfNotNull(application.lastAddedEntity)))
        InteractionType.Navigation,
        InteractionType.SpecialNavigation,
        InteractionType.SelectSpatial,
        InteractionType.SelectTemporal,
        InteractionType.Action -> this
    }

    override fun SketchUpModel.handleCompleteDrag(interaction: InteractionType, path: Path<ScreenSpace>) = when (interaction) {
        InteractionType.Sketch -> addPathEntity(newPath(path, application.viewport))
        InteractionType.SelectSpatial -> selectEntities(path)
        InteractionType.Navigation -> translateViewport(-path.offset.transformS(application.viewport.screenToWorld))
        InteractionType.SpecialNavigation,
        InteractionType.SelectTemporal,
        InteractionType.SelectMeta,
        InteractionType.Action -> this
    }

    override fun SketchUpModel.handlePartialDrag(interaction: InteractionType, path: Path<ScreenSpace>) = when (interaction) {
        InteractionType.SelectSpatial -> setSelectionHint(path.startPoint, path.endPoint).selectEntities(path)
        InteractionType.Navigation -> translateViewport(-path.offset.transformS(application.viewport.screenToWorld))
        InteractionType.SpecialNavigation,
        InteractionType.SelectTemporal,
        InteractionType.SelectMeta,
        InteractionType.Action,
        InteractionType.Sketch
        -> setPathHint(path)
    }
}

private fun SketchUpModel.addPointEntity(point: Point<WorldSpace>) =
    addEntity(PointEntity(point, client.colour))

private fun SketchUpModel.addPathEntity(path: Path<WorldSpace>) =
    addEntity(PathEntity(path, client.colour)).addEntity(PathEntity(path.approx(), client.colour))

internal fun SketchUpModel.setPathHint(path: Path<ScreenSpace>) =
    copy(application = application.copy(visualHint = VisualHint.path(path))).deselect()

internal fun SketchUpModel.setSelectionHint(first: PathPoint<ScreenSpace>, second: PathPoint<ScreenSpace>) =
    copy(application = application.copy(visualHint = VisualHint.rectangularSelection((Path.of(first) + Path.of(second)).boundingArea))).deselect()

internal fun SketchUpModel.addEntity(entity: Entity) = copy(
    sketch = sketch.copy(snapshot = sketch.snapshot.copy(entities = sketch.snapshot.entities.add(entity))),
    application = application.copy(lastAddedEntity = entity),
).deselect()

internal fun SketchUpModel.deselect() =
    copy(application = application.copy(selectedEntities = emptySet()))

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
