package com.exerro.sketchup

import com.exerro.sketchup.data.*
import com.exerro.sketchup.impl.BasicEntitySetImpl
import com.exerro.sketchup.impl.NanoVGRenderer
import com.exerro.sketchup.impl.createGLFWWindowSystem
import com.exerro.sketchup.impl.initialiseNanoVG
import com.exerro.sketchup.util.boundingArea

data class SketchUpModel(
    val entities: EntitySet,
    val viewport: Viewport,
    val pointer: Vector<ScreenSpace>,
    val visualHint: VisualHint,
) {
    companion object {
        val blank = SketchUpModel(
            BasicEntitySetImpl.empty,
            Viewport(WorldPosition(0.0, 0.0), 0.0, WindowSize(0.0, 0.0)),
            Vector.origin(),
            VisualHint.None,
        )
    }
}

sealed class VisualHint {
    object None: VisualHint()
    data class PathHint(val path: Path<ScreenSpace>): VisualHint()
    data class SelectionHint(val bounds: BoundingArea<ScreenSpace>): VisualHint()
}

typealias SketchUpMessage = WindowEvent

////////////////////////////////////////////////////////////////////////////////

private fun colour(mode: PointerMode, alternate: Boolean): Colour {
    val colour = when (mode) {
        PointerMode.Primary -> Colour.red
        PointerMode.Tertiary -> Colour.blue
        PointerMode.Secondary -> Colour.green
    }
    return if (alternate) colour.lighten(0.3f) else colour
}

private fun updateViewportWindowSize(model: SketchUpModel, size: Vector<ScreenSpace>) =
    model.copy(viewport = model.viewport.copy(windowSize = size))

private fun newPoint(point: Point<ScreenSpace>, viewport: Viewport) =
    Point(point.position, point.size * 8.0).transform(viewport.screenToWorld)

private fun addPoint(model: SketchUpModel, point: Point<WorldSpace>, colour: Colour) =
    model.copy(entities = model.entities.add(PointEntity(point, colour)))

private fun newPath(path: Path<ScreenSpace>, viewport: Viewport) =
    Path(path.points.map { Point(it.position, it.size * 4.0).transform(viewport.screenToWorld) })

private fun addPath(model: SketchUpModel, path: Path<WorldSpace>, colour: Colour) =
    model.copy(entities = model.entities.add(PathEntity(path, colour)))

private fun setPathHint(model: SketchUpModel, path: Path<ScreenSpace>) =
    model.copy(visualHint = VisualHint.PathHint(path))

private fun setSelectionHint(model: SketchUpModel, first: Point<ScreenSpace>, second: Point<ScreenSpace>) =
    model.copy(visualHint = VisualHint.SelectionHint((Path.of(first) + Path.of(second)).boundingArea))

private fun translateViewport(model: SketchUpModel, translation: Vector<WorldSpace>) =
    model.copy(viewport = model.viewport.copy(centre = model.viewport.centre + translation))

private fun adjustViewportScale(model: SketchUpModel, adjustment: Double) =
    model.copy(viewport = model.viewport.copy(scale = model.viewport.scale + adjustment))

fun updateModel(model: SketchUpModel, message: SketchUpMessage): SketchUpModel = when (message) {
    is RedrawEvent -> updateViewportWindowSize(model, message.windowSize)
    is PointerPressEvent -> addPoint(model, newPoint(message.point, model.viewport), colour(message.mode, message.alternate))
    is PointerDragEvent -> when (message.mode) {
        PointerMode.Primary -> when {
            message.complete -> addPath(model, newPath(message.path, model.viewport), colour(message.mode, message.alternate))
            else -> setPathHint(model, message.path)
        }
        PointerMode.Secondary -> when { // TODO! selection box
            message.complete -> model // TODO! select entities
            else -> setSelectionHint(model, message.path.first, message.path.last)
        }
        PointerMode.Tertiary -> translateViewport(model, -message.path.offset.transformS(model.viewport.screenToWorld))
    }
    is ScrollEvent -> when (message.mode) {
        ScrollMode.Primary -> translateViewport(model, -message.delta.transformS(model.viewport.screenToWorld) * 64.0)
        ScrollMode.Secondary -> adjustViewportScale(model, message.delta.y)
    }
    is KeyEvent, is InputEvent, is PointerMoveEvent -> model
}

fun NanoVGRenderer.drawModel(model: SketchUpModel) = draw(
    model.viewport.windowSize.x.toInt(),
    model.viewport.windowSize.y.toInt()
) {
    model.entities.allVisible(model.viewport).forEach {
        it.run { draw(model.viewport) }
        boxOutline(it.boundingArea.transform(model.viewport.worldToScreen), Colour.white)
    }
    when (model.visualHint) {
        is VisualHint.PathHint -> path(model.visualHint.path, Colour.lighterGrey)
        is VisualHint.SelectionHint -> boxOutline(model.visualHint.bounds, Colour.grey)
        is VisualHint.None -> {}
    }
}

////////////////////////////////////////////////////////////////////////////////

fun main() {
    val windowing = createGLFWWindowSystem()
    val events = windowing.createWindow(WindowSettings())
    val vg = initialiseNanoVG()
    val renderer = NanoVGRenderer.create(vg)
    val connections = mutableListOf<ObservableStreamConnection>()
    val models = events.eventTransformedFold(SketchUpModel.blank, ::updateModel)

    connections.add(models)
    connections.add(models.connect(renderer::drawModel))

    windowing.runBlocking()

    connections.forEach(ObservableStreamConnection::disconnect)
}
