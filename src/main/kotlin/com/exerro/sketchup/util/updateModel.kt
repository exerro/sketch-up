package com.exerro.sketchup.util

import com.exerro.sketchup.Entity
import com.exerro.sketchup.SketchUpModel
import com.exerro.sketchup.VisualHint
import com.exerro.sketchup.data.*

fun SketchUpModel.updateViewportWindowSize(size: Vector<ScreenSpace>) =
    copy(viewport = viewport.copy(windowSize = size))

fun SketchUpModel.translateViewport(translation: Vector<WorldSpace>) =
    copy(viewport = viewport.copy(centre = viewport.centre + translation))

fun SketchUpModel.adjustViewportScale(adjustment: Double) =
    copy(viewport = viewport.copy(scale = viewport.scale + adjustment))

fun SketchUpModel.setPathHint(path: Path<ScreenSpace>) =
    copy(visualHint = VisualHint.PathHint(path)).deselect()

fun SketchUpModel.setSelectionHint(first: Point<ScreenSpace>, second: Point<ScreenSpace>) =
    copy(visualHint = VisualHint.SelectionHint((Path.of(first) + Path.of(second)).boundingArea)).deselect()

fun SketchUpModel.addEntity(entity: Entity) =
    copy(entities = entities.add(entity), lastAddedEntity = entity).deselect()

fun SketchUpModel.deselect() =
    copy(selectedEntities = emptySet())
