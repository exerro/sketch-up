package com.exerro.sketchup.util

import com.exerro.sketchup.Entity
import com.exerro.sketchup.SketchUpModel
import com.exerro.sketchup.VisualHint
import com.exerro.sketchup.data.*

fun SketchUpModel.updateViewportWindowSize(size: Vector<ScreenSpace>) =
    copy(application = application.copy(viewport = application.viewport.copy(windowSize = size)))

fun SketchUpModel.translateViewport(translation: Vector<WorldSpace>) =
    copy(application = application.copy(viewport = application.viewport.copy(focus = application.viewport.focus.copy(centre = application.viewport.focus.centre + translation))))

fun SketchUpModel.adjustViewportScale(adjustment: Double) =
    copy(application = application.copy(viewport = application.viewport.copy(focus = application.viewport.focus.copy(scale = application.viewport.focus.scale + adjustment))))

fun SketchUpModel.setPathHint(path: Path<ScreenSpace>) =
    copy(application = application.copy(visualHint = VisualHint.path(path))).deselect()

fun SketchUpModel.setSelectionHint(first: Point<ScreenSpace>, second: Point<ScreenSpace>) =
    copy(application = application.copy(visualHint = VisualHint.rectangularSelection((Path.of(first) + Path.of(second)).boundingArea))).deselect()

fun SketchUpModel.addEntity(entity: Entity) = copy(
    sketch = sketch.copy(snapshot = sketch.snapshot.copy(entities = sketch.snapshot.entities.add(entity))),
    application = application.copy(lastAddedEntity = entity),
).deselect()

fun SketchUpModel.deselect() =
    copy(application = application.copy(selectedEntities = emptySet()))
