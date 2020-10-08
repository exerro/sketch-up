package com.exerro.sketchup.api

import com.exerro.sketchup.application.SketchUpModel

abstract class Action internal constructor() {
    internal abstract fun SketchUpModel.apply(): SketchUpModel
}

class AddEntityAction(
    private val fn: AddEntityContext.() -> Entity
): Action() {
    override fun SketchUpModel.apply() = copy(
        sketch = sketch.copy(
            snapshot = sketch.snapshot.copy(
                entities = sketch.snapshot.entities.add(fn(object: AddEntityContext {

                }))
            )
        )
    )
}

interface AddEntityContext
