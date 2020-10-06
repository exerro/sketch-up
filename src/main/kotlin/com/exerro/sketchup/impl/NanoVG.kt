package com.exerro.sketchup.impl

import org.lwjgl.nanovg.NanoVG
import org.lwjgl.nanovg.NanoVGGL3.*

fun initialiseNanoVG(): Long {
    val nvgContext = nvgCreate(NVG_ANTIALIAS or NVG_STENCIL_STROKES or NVG_DEBUG)
    NanoVG.nvgFontSize(nvgContext, 22f)
    NanoVG.nvgFontBlur(nvgContext, 0f)
    return nvgContext
}
