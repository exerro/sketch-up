package com.exerro.sketchup.impl

import com.exerro.sketchup.api.DrawContext
import com.exerro.sketchup.api.Window
import com.exerro.sketchup.api.WindowSystem
import com.exerro.sketchup.api.data.*
import com.exerro.sketchup.api.streams.ObservableStream
import com.exerro.sketchup.api.util.StreamConnectionManager
import org.lwjgl.glfw.GLFW.*
import org.lwjgl.glfw.GLFWErrorCallback
import org.lwjgl.opengl.GL
import org.lwjgl.system.MemoryUtil
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.BlockingQueue
import kotlin.concurrent.thread

internal fun createGLFWWindowSystem() = object: WindowSystem {
    override fun createWindow(settings: WindowSettings): Window {
        val id = glfwCreateWindow(settings)
        synchronized(windows) { windows.add(id) }
        val events = glfwHookEvents(id)
        val worker = WorkerThread(id)
        val render = RenderThread(id)

        val eventStream = ObservableStream<ExtendedWindowEvent> { onItem ->
            events.connect { item -> worker.submit { onItem(item) } }
        }

        return object: Window {
            override val events get() = eventStream

            override fun close() {
                glfwSetWindowShouldClose(id, true)
                worker.stop()
                render.stop()
            }

            override fun draw(draw: DrawContext.() -> Unit) =
                render.submit(draw)
        }
    }

    override fun runBlocking() {
        while (true) {
            val anyRemaining = synchronized(windows) {
                windows.filter(::glfwWindowShouldClose).forEach {
                    glfwDestroyWindow(it)
                    windows.remove(it)
                }
                windows.isNotEmpty()
            }

            if (!anyRemaining) break

            glfwWaitEventsTimeout(0.05)
        }

        glfwTerminate()
        glfwSetErrorCallback(null)?.free()
    }

    private val windows = mutableListOf<Long>()

    init {
        glfwSetErrorCallback(GLFWErrorCallback.createPrint(System.err))
        if (!glfwInit()) error("Failed to initialise GLFW")

        glfwWindowHint(GLFW_CLIENT_API, GLFW_OPENGL_API)
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE)
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE)
        glfwWindowHint(GLFW_TRANSPARENT_FRAMEBUFFER, GLFW_FALSE)
        glfwWindowHint(GLFW_FOCUS_ON_SHOW, GLFW_TRUE)
    }
}

////////////////////////////////////////////////////////////////////////////////

/** Create a window. */
private fun glfwCreateWindow(settings: WindowSettings): Long {
    val windowID = glfwCreateWindow(settings.width, settings.height, settings.title, MemoryUtil.NULL, MemoryUtil.NULL)
        .takeIf { it != MemoryUtil.NULL }
        ?: error("Failed to create GLFW window.")

    run { // centre the window within the monitor it's in, and show it
        val monitorID = glfwGetPrimaryMonitor().takeIf { it != MemoryUtil.NULL } ?: error("Failed to get primary monitor")
        val mode = glfwGetVideoMode(monitorID) ?: error("Failed to get primary monitor video mode")
        val mx = IntArray(1); val my = IntArray(1) // monitor virtual position
        val ww = IntArray(1); val wh = IntArray(1) // window size

        glfwGetMonitorPos(monitorID, mx, my)
        glfwGetWindowSize(windowID, ww, wh)
        glfwSetWindowPos(windowID, mx[0] + (mode.width() - ww[0]) / 2, my[0] + (mode.height() - wh[0]) / 2)
        glfwShowWindow(windowID)
    }

    return windowID
}

private fun glfwSetupWindowGL(windowID: Long) {
    glfwMakeContextCurrent(windowID)
    glfwSwapInterval(1)
    GL.createCapabilities()
}

/** Create an event stream for window events. */
private fun glfwHookEvents(windowID: Long): ObservableStream<ExtendedWindowEvent> {
    val subscriptions = StreamConnectionManager<ExtendedWindowEvent>()
    val wb = IntArray(1)
    val hb = IntArray(1)
    glfwGetFramebufferSize(windowID, wb, hb)
    var width = wb[0].toDouble()
    var height = hb[0].toDouble()
    var heldButton = null as Int?
    var heldPointerMode: PointerMode

    glfwSetKeyCallback(windowID) { _, key, scancode, action, mods ->
        if (key == GLFW_KEY_V && (mods and GLFW_MOD_CONTROL) != 0) {
            if (action == GLFW_PRESS) {
                val text = glfwGetClipboardString(MemoryUtil.NULL)
                text ?.let { subscriptions.invoke(InputEvent(GenericInput.Text(it))) }
            }
        }
        else {
            val keyCombination = KeyCombination.fromGLFW(key, scancode, mods)
            keyCombination ?.let { subscriptions.invoke(KeyEvent(action == GLFW_RELEASE, action == GLFW_PRESS, it)) }
        }
    }

    glfwSetMouseButtonCallback(windowID) { _, button, action, _ ->
        val xb = DoubleArray(1)
        val yb = DoubleArray(1)
        glfwGetCursorPos(windowID, xb, yb)
        val position = Vector<ScreenSpace>(xb[0], yb[0])
        heldPointerMode = when (button) {
            GLFW_MOUSE_BUTTON_1 -> PointerMode.Primary
            GLFW_MOUSE_BUTTON_2 -> PointerMode.Secondary
            else -> PointerMode.Tertiary
        }

        if (action == GLFW_PRESS) {
            if (heldButton == null) {
                subscriptions.invoke(PointerPressedEvent(heldPointerMode, position, pressure))
                heldButton = button
            }
        }
        else if (action == GLFW_RELEASE) {
            if (heldButton == button) {
                subscriptions.invoke(PointerReleasedEvent)
                heldButton = null
            }
        }
    }

    glfwSetCursorPosCallback(windowID) { _, x, y ->
        val position = Vector<ScreenSpace>(x, y)
        subscriptions.invoke(PointerMoveEvent(position, pressure))
    }

    glfwSetScrollCallback(windowID) { _, dx, dy ->
        val lctrl = glfwGetKey(windowID, GLFW_KEY_LEFT_CONTROL) == GLFW_TRUE
        val rctrl = glfwGetKey(windowID, GLFW_KEY_RIGHT_CONTROL) == GLFW_TRUE
        val mode = if (lctrl || rctrl) ScrollMode.Secondary else ScrollMode.Primary
        subscriptions.invoke(ScrollEvent(mode, Vector(dx, dy)))
    }

    glfwSetCharCallback(windowID) { _, codepoint ->
        subscriptions.invoke(InputEvent(GenericInput.Text(String(Character.toChars(codepoint)))))
    }

    glfwSetDropCallback(windowID) { _, count, pathsPtr ->
        val pathsBuffer = MemoryUtil.memPointerBuffer(pathsPtr, count)
        val paths = (0 until count).map { MemoryUtil.memUTF8(MemoryUtil.memByteBufferNT1(pathsBuffer[it])) }
        subscriptions.invoke(InputEvent(GenericInput.Files(paths)))
    }

    glfwSetWindowRefreshCallback(windowID) {
        subscriptions.invoke(RedrawEvent(Vector(width, height)))
    }

    glfwSetFramebufferSizeCallback(windowID) { _, w, h ->
        width = w.toDouble()
        height = h.toDouble()
        subscriptions.invoke(RedrawEvent(Vector(width, height)))
    }

    return ObservableStream(subscriptions::add)
}

private val pressure = Scalar<ScreenSpace>(1.0)

class WorkerThread(
    windowID: Long
) {
    fun submit(fn: () -> Unit) {
        queue.put(fn)
    }

    fun stop() {
        running = false
        queue.put {}
    }

    init {
        thread(name = "Worker $windowID", start = true, isDaemon = true) {
            queue = ArrayBlockingQueue(1024)
            while (running) queue.take()()
        }
    }

    private var running = true
    private lateinit var queue: BlockingQueue<() -> Unit>
}

class RenderThread(
    private val windowID: Long
) {
    fun submit(fn: DrawContext.() -> Unit) {
        synchronized(drawLock) { draw = fn }
    }

    fun stop() {
        running = false
    }

    init {
        thread(name = "Render $windowID", start = true, isDaemon = true) {
            glfwSetupWindowGL(windowID)
            graphics = NanoVGRenderer.create()

            while (running) {
                synchronized(drawLock) { draw.also { draw = null } }
                    ?.let { draw ->
                        val width = IntArray(1)
                        val height = IntArray(1)
                        glfwGetFramebufferSize(windowID, width, height)
                        graphics.draw(width[0], height[0], draw)
                        glfwSwapBuffers(windowID)
                    }
            }
        }
    }

    private var running = true
    private var draw: (DrawContext.() -> Unit)? = null
    private val drawLock = Object()
    private lateinit var graphics: NanoVGRenderer
}
