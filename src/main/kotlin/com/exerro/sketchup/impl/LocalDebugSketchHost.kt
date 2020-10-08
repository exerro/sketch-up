package com.exerro.sketchup.impl

import com.exerro.sketchup.api.SketchHost
import com.exerro.sketchup.application.SketchSnapshot

internal object LocalDebugSketchHost: SketchHost {
    override fun constantSnapshot() = SketchSnapshot(BasicEntitySetImpl.empty)
}
