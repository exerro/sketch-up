package com.exerro.sketchup

import org.lwjgl.glfw.GLFW.glfwGetFramebufferSize
import org.lwjgl.glfw.GLFW.glfwSwapBuffers
import org.lwjgl.nanovg.NVGColor
import org.lwjgl.nanovg.NanoVG.*
import org.lwjgl.opengl.GL46C.*

interface RendererDrawContext {
    fun point(position: WindowPosition, size: Float, colour: Colour)
    fun ipath(path: Path<ScreenSpace>, colour: Colour)
}

class Renderer private constructor(
    private val vg: Long,
    private val glfwWindowID: Long,
) {
    val colourBuffer = NVGColor.malloc()

    @Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE")
    fun draw(draw: RendererDrawContext.() -> Unit) {
        val framebufferWidth = IntArray(1)
        val framebufferHeight = IntArray(1)
        glfwGetFramebufferSize(glfwWindowID, framebufferWidth, framebufferHeight)

        nvgRGBA(255.toByte(), 192.toByte(), 0.toByte(), 255.toByte(), colourBuffer)

        glViewport(0, 0, framebufferWidth[0], framebufferHeight[0])
        glClearColor(0x2e / 256f, 0x34 / 256f, 0x40 / 256f, 1f)
        glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT or GL_STENCIL_BUFFER_BIT)
        nvgBeginFrame(vg, framebufferWidth[0].toFloat(), framebufferHeight[0].toFloat(), framebufferWidth[0].toFloat() / framebufferHeight[0])

        draw(object: RendererDrawContext {
            override fun point(position: WindowPosition, size: Float, colour: Colour) {
                nvgRGBA(colour.red.toByteComponent(), colour.green.toByteComponent(), colour.blue.toByteComponent(), colour.alpha.toByteComponent(), colourBuffer)
                nvgBeginPath(vg)
                nvgCircle(vg, position.x.toFloat(), position.y.toFloat(), size)
                nvgFillColor(vg, colourBuffer)
                nvgFill(vg)
            }

            override fun ipath(path: Path<ScreenSpace>, colour: Colour) {
                if (path.points.isEmpty()) return
                nvgRGBA(colour.red.toByteComponent(), colour.green.toByteComponent(), colour.blue.toByteComponent(), colour.alpha.toByteComponent(), colourBuffer)
                nvgBeginPath(vg)
                nvgMoveTo(vg, path.points.first().position.x.toFloat(), path.points.first().position.y.toFloat())
                path.points.forEach { (pos, _) ->
                    nvgLineTo(vg, pos.x.toFloat(), pos.y.toFloat())
                }
                nvgStrokeWidth(vg, 3f)
                nvgStrokeColor(vg, colourBuffer)
//                nvgFillColor(vg, colourBuffer)
//                nvgClosePath(vg)
//                nvgFill(vg)
                nvgStroke(vg)
            }
        })

        nvgEndFrame(vg)
        glfwSwapBuffers(glfwWindowID)
    }

    companion object {
        fun create(glfwWindowID: Long, vg: Long) = Renderer(vg, glfwWindowID)
    }
}

data class RendererDrawCommand(
    val viewportWidth: Int,
    val viewportHeight: Int,
    val vgWidth: Float,
    val vgHeight: Float,
    val draw: List<RendererDrawCall>
)

sealed class RendererDrawCall {
    data class Point(val position: WindowPosition, val size: Float): RendererDrawCall()
}

private fun Float.toByteComponent() = (this * 255).toInt().toByte()
