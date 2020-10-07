package com.exerro.sketchup.impl.adapters

import com.exerro.sketchup.*
import com.exerro.sketchup.data.*
import com.exerro.sketchup.util.addEntity
import com.exerro.sketchup.util.boundingArea
import com.exerro.sketchup.util.setPathHint
import com.exerro.sketchup.util.setSelectionHint

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
    addEntity(PathEntity(path, colour))

private fun SketchUpModel.selectEntities(path: Path<ScreenSpace>): SketchUpModel {
    val selectedArea = (Path.of(path.first) + Path.of(path.last)).boundingArea.transform(application.viewport.screenToWorld)
    val selected = sketch.snapshot.entities.allVisible(application.viewport).filter { it.boundingArea in selectedArea }
    return copy(application = application.copy(selectedEntities = selected.toSet()))
}

private fun newPoint(point: Point<ScreenSpace>, viewport: Viewport) =
    Point(point.position, point.size * POINT_SIZE).transform(viewport.screenToWorld)

private fun newPath(path: Path<ScreenSpace>, viewport: Viewport) =
    Path(path.points.map { Point(it.position, it.size * LINE_WIDTH).transform(viewport.screenToWorld) })

const val POINT_SIZE = 5.0
const val LINE_WIDTH = 2.0
