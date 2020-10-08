package com.exerro.sketchup.impl

import com.exerro.sketchup.api.DrawContext
import com.exerro.sketchup.api.data.*
import org.lwjgl.nanovg.NVGColor
import org.lwjgl.nanovg.NanoVG
import org.lwjgl.nanovg.NanoVG.*
import org.lwjgl.nanovg.NanoVGGL3
import org.lwjgl.opengl.GL46C.*
import kotlin.math.PI
import kotlin.math.asin

internal class NanoVGRenderer private constructor(
    private val vg: Long,
) {

    @Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE")
    fun draw(framebufferWidth: Int, framebufferHeight: Int, draw: DrawContext.() -> Unit) {
        glViewport(0, 0, framebufferWidth, framebufferHeight)
        glClearColor(0x32 / 256f, 0x37 / 256f, 0x40 / 256f, 1f)
        glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT or GL_STENCIL_BUFFER_BIT)
        nvgBeginFrame(vg, framebufferWidth.toFloat(), framebufferHeight.toFloat(), framebufferWidth.toFloat() / framebufferHeight)
        VGDrawContext(vg).draw()
        nvgEndFrame(vg)
    }

    companion object {
        fun create(): NanoVGRenderer {
            val nvgContext = NanoVGGL3.nvgCreate(NanoVGGL3.NVG_ANTIALIAS or NanoVGGL3.NVG_STENCIL_STROKES or NanoVGGL3.NVG_DEBUG)
            nvgFontSize(nvgContext, 22f)
            nvgFontBlur(nvgContext, 0f)
            return NanoVGRenderer(nvgContext)
        }
    }
}

private class VGDrawContext(
    private val vg: Long
): DrawContext {
    private val colourBuffer = NVGColor.malloc()

    override fun point(point: Point<ScreenSpace>, colour: Colour) {
        beginPath(colour, fill = true)
        nvgCircle(vg, point.position.x.toFloat(), point.position.y.toFloat(), point.size.value.toFloat())
        nvgFill(vg)
    }

    override fun path(path: Path<ScreenSpace>, colour: Colour) {
        if (path.isEmpty()) return

         path.points.drop(1).zip(path.points).forEach { (a, b) ->
            if (a.size.value < b.size.value) drawConnectingSegment(smaller = a, larger = b, colour)
            else drawConnectingSegment(smaller = b, larger = a, colour)
        }

        path.points.forEach {
            beginPath(colour, fill = true)
            nvgCircle(vg, it.position.x.toFloat(), it.position.y.toFloat(), it.size.value.toFloat())
            nvgFill(vg)
        }
    }

    override fun boxOutline(boundingArea: BoundingArea<ScreenSpace>, colour: Colour) {
        beginPath(colour, fill = false)
        nvgRect(vg, boundingArea.xMin.toFloat(), boundingArea.yMin.toFloat(), boundingArea.width.toFloat(), boundingArea.height.toFloat())
        nvgStrokeWidth(vg, 1f)
        nvgStroke(vg)
    }

    private fun drawConnectingSegment(smaller: PathPoint<ScreenSpace>, larger: PathPoint<ScreenSpace>, colour: Colour) {
        val rs = smaller.size.value
        val rl = larger.size.value
        val delta = larger.position - smaller.position
        val d = delta.magnitude
        val unitDelta = delta / d
        val rsds = rs * (rl - rs) / (d * rs)
        val theta = asin(rsds)
        val ccw = unitDelta.rotate(theta + PI / 2)
        val cw = unitDelta.rotate(-theta - PI / 2)
        val p0 = smaller.position + ccw * rs
        val p1 = smaller.position + cw * rs
        val p2 = larger.position + cw * rl
        val p3 = larger.position + ccw * rl

        beginPath(colour, fill = true)
        nvgMoveTo(vg, p0.x.toFloat(), p0.y.toFloat())
        nvgLineTo(vg, p1.x.toFloat(), p1.y.toFloat())
        nvgLineTo(vg, p2.x.toFloat(), p2.y.toFloat())
        nvgLineTo(vg, p3.x.toFloat(), p3.y.toFloat())
        nvgFill(vg)
    }

    private fun beginPath(colour: Colour, fill: Boolean) {
        nvgBeginPath(vg)
        nvgRGBA(
            colour.red.toByteComponent(),
            colour.green.toByteComponent(),
            colour.blue.toByteComponent(),
            colour.alpha.toByteComponent(),
            colourBuffer
        )
        if (fill) nvgFillColor(vg, colourBuffer)
        else nvgStrokeColor(vg, colourBuffer)
    }
}

private fun Float.toByteComponent() = (this * 255).toInt().toByte()
