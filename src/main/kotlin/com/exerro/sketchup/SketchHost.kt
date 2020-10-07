package com.exerro.sketchup

import com.exerro.sketchup.impl.BasicEntitySetImpl
import com.exerro.sketchup.model.SketchSnapshot

interface SketchHost {
    @Deprecated("Will be replaced by a streamed model snapshot")
    fun constantSnapshot(): SketchSnapshot // TODO: replace event system to support this
}

object LocalDebugSketchHost: SketchHost {
    override fun constantSnapshot() = SketchSnapshot(BasicEntitySetImpl.empty)
}
