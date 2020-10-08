package com.exerro.sketchup.api

import com.exerro.sketchup.application.SketchSnapshot

interface SketchHost {
    @Deprecated("Will be replaced by a streamed model snapshot")
    fun constantSnapshot(): SketchSnapshot // TODO: replace event system to support this
}
