package com.exerro.sketchup

import org.lwjgl.glfw.GLFW.*
import org.lwjgl.glfw.GLFWErrorCallback
import org.lwjgl.opengl.GL
import org.lwjgl.system.MemoryUtil

/** Set up GLFW and an appropriate window. */
fun initialiseGLFW() {
    glfwSetErrorCallback(GLFWErrorCallback.createPrint(System.err))
    if (!glfwInit()) error("Failed to initialise GLFW")

    glfwWindowHint(GLFW_CLIENT_API, GLFW_OPENGL_API)
    glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE)
    glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE)
    glfwWindowHint(GLFW_TRANSPARENT_FRAMEBUFFER, GLFW_FALSE)
    glfwWindowHint(GLFW_FOCUS_ON_SHOW, GLFW_TRUE)
}

/** Create a window. */
fun glfwCreateWindow(width: Int, height: Int, title: String): Long {
    val windowID = glfwCreateWindow(width, height, title, MemoryUtil.NULL, MemoryUtil.NULL)
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

    glfwMakeContextCurrent(windowID)
    glfwSwapInterval(1)
    GL.createCapabilities()

    return windowID
}

/** Create an event stream for window events. */
fun glfwHookEvents(windowID: Long): ObservableStream<WindowEvent> {
    val subscriptions = ConnectionManager<WindowEvent>()
    val pm = PointerManager()
    val wb = IntArray(1)
    val hb = IntArray(1)
    glfwGetFramebufferSize(windowID, wb, hb)
    var width = wb[0].toFloat()
    var height = hb[0].toFloat()

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
        val position = WindowPosition(xb[0], yb[0])

        if (action == GLFW_PRESS)
            pm.press(position, button)
        else if (action == GLFW_RELEASE)
            pm.release(position)?.let(subscriptions::invoke)
    }

    glfwSetCursorPosCallback(windowID) { _, x, y ->
        pm.move(WindowPosition(x, y))?.let(subscriptions::invoke)
    }

    glfwSetScrollCallback(windowID) { _, dx, dy ->
        val lctrl = glfwGetKey(windowID, GLFW_KEY_LEFT_CONTROL) == GLFW_TRUE
        val rctrl = glfwGetKey(windowID, GLFW_KEY_RIGHT_CONTROL) == GLFW_TRUE
        val mode = if (lctrl || rctrl) ScrollMode.Secondary else ScrollMode.Primary
        subscriptions.invoke(ScrollEvent(mode, Vector<ScreenSpace>(dx, dy)))
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
        subscriptions.invoke(RedrawEvent(width, height))
    }

    glfwSetFramebufferSizeCallback(windowID) { _, w, h ->
        width = w.toFloat()
        height = h.toFloat()
        subscriptions.invoke(RedrawEvent(width, height))
    }

    return ObservableStream(subscriptions::add)
}

/** Close the window. */
fun glfwCloseWindow(windowID: Long) {
    glfwSetWindowShouldClose(windowID, true)
}

/** Terminate GLFW. */
fun terminateGLFW() {
    glfwTerminate()
    glfwSetErrorCallback(null)?.free()
}

////////////////////////////////////////////////////////////////////////////////

private class PointerManager {
    sealed class State {
        object Init: State()

        data class ReadyForDoublePress(
            val position: WindowPosition,
            val time: Double,
            val doubleWillInvalidate: WindowEvent,
        ): State()

        sealed class HeldState: State() {
            abstract val mode: PointerMode
            abstract val firstPosition: WindowPosition
            abstract val updateWillInvalidate: WindowEvent?

            data class Held(
                override val mode: PointerMode,
                override val firstPosition: WindowPosition,
                override val updateWillInvalidate: WindowEvent?
            ): HeldState()

            data class Dragged(
                override val mode: PointerMode,
                override val firstPosition: WindowPosition,
                override val updateWillInvalidate: WindowEvent?,
                val positions: List<WindowPosition> = listOf(firstPosition),
            ): HeldState()
        }
    }

    fun press(position: WindowPosition, button: Int) {
        val state = this.state
        val (mode, updateWillInvalidate) = when {
            button != GLFW_MOUSE_BUTTON_LEFT -> PointerMode.Alternate to null
            state !is State.ReadyForDoublePress -> PointerMode.Primary to null
            isDoublePress(state, position) -> PointerMode.Secondary to state.doubleWillInvalidate
            else -> PointerMode.Primary to null
        }

        this.state = State.HeldState.Held(mode, position, updateWillInvalidate)
    }

    fun release(position: WindowPosition): WindowEvent? = when (val state = this.state) {
        is State.HeldState.Held -> {
            val event = PointerPressEvent(state.mode, position, invalidates = state.updateWillInvalidate)

            this.state = when (state.mode) {
                PointerMode.Primary -> State.ReadyForDoublePress(position, glfwGetTime(), event)
                else -> State.Init
            }

            event
        }
        is State.HeldState.Dragged -> {
            this.state = State.Init

            PointerDragEvent(
                state.mode,
                PointerDrag(state.firstPosition, position, state.positions),
                ongoing = false,
                invalidates = state.updateWillInvalidate
            )
        }
        else -> null
    }

    fun move(position: WindowPosition): WindowEvent? {
        val state = this.state as? State.HeldState ?: return null
        val positions = when (state) {
            is State.HeldState.Held -> listOf(state.firstPosition, position)
            is State.HeldState.Dragged -> state.positions + position
        }
        val event = PointerDragEvent(
            state.mode,
            PointerDrag(state.firstPosition, position, positions),
            ongoing = true,
            state.updateWillInvalidate
        )

        this.state = State.HeldState.Dragged(state.mode, state.firstPosition, event, positions)
        return event
    }

    private var state = State.Init as State

    companion object {
        const val TIMEOUT = 0.2
    }
}

private fun isDoublePress(state: PointerManager.State.ReadyForDoublePress, position: WindowPosition) =
    glfwGetTime() <= state.time + PointerManager.TIMEOUT && !movedTooFar(state.position, position)

private fun movedTooFar(a: WindowPosition, b: WindowPosition) =
    (a.x - b.x) * (a.x - b.x) + (a.y - b.y) * (a.y - b.y) > MOVEMENT_THRESHOLD_SQUARED

private const val MOVEMENT_THRESHOLD = 30f
private const val MOVEMENT_THRESHOLD_SQUARED = MOVEMENT_THRESHOLD * MOVEMENT_THRESHOLD
