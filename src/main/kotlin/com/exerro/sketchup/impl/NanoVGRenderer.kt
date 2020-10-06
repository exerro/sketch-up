package com.exerro.sketchup.impl

import com.exerro.sketchup.DrawContext
import com.exerro.sketchup.data.*
import org.lwjgl.nanovg.NVGColor
import org.lwjgl.nanovg.NanoVG.*
import org.lwjgl.opengl.GL46C.*

class NanoVGRenderer private constructor(
    private val vg: Long,
) {
    private val colourBuffer = NVGColor.malloc()

    @Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE")
    fun draw(framebufferWidth: Int, framebufferHeight: Int, draw: DrawContext.() -> Unit) {
        nvgRGBA(255.toByte(), 192.toByte(), 0.toByte(), 255.toByte(), colourBuffer)

        glViewport(0, 0, framebufferWidth, framebufferHeight)
        glClearColor(0x2e / 256f, 0x34 / 256f, 0x40 / 256f, 1f)
        glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT or GL_STENCIL_BUFFER_BIT)
        nvgBeginFrame(vg, framebufferWidth.toFloat(), framebufferHeight.toFloat(), framebufferWidth.toFloat() / framebufferHeight)

        draw(object : DrawContext {
            override fun point(point: Point<ScreenSpace>, colour: Colour) {
                beginPath(colour, fill = true)
                nvgCircle(vg, point.position.x.toFloat(), point.position.y.toFloat(), point.size.value.toFloat())
                nvgFill(vg)
            }

            override fun path(path: Path<ScreenSpace>, colour: Colour) {
                if (path.points.isEmpty()) return
                beginPath(colour, fill = false)
                nvgMoveTo(vg, path.points.first().position.x.toFloat(), path.points.first().position.y.toFloat())
                path.points.forEach { (pos, _) ->
                    nvgLineTo(vg, pos.x.toFloat(), pos.y.toFloat())
                }
                nvgStrokeWidth(vg, 3f)
//                nvgFillColor(vg, colourBuffer)
//                nvgClosePath(vg)
//                nvgFill(vg)
                nvgStroke(vg)
            }

            override fun boxOutline(boundingArea: BoundingArea<ScreenSpace>, colour: Colour) {
                beginPath(colour, fill = false)
                nvgRect(vg, boundingArea.xMin.toFloat(), boundingArea.yMin.toFloat(), boundingArea.width.toFloat(), boundingArea.height.toFloat())
                nvgStrokeWidth(vg, 1f)
                nvgStroke(vg)
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
        })

        nvgEndFrame(vg)
    }

    companion object {
        fun create(vg: Long) = NanoVGRenderer(vg)
    }
}

private fun Float.toByteComponent() = (this * 255).toInt().toByte()
