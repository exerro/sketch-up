import org.lwjgl.glfw.GLFW.*
import org.lwjgl.glfw.GLFWErrorCallback
import org.lwjgl.system.MemoryUtil

/** Set up GLFW and an appropriate window. */
fun initialiseGLFW(width: Int, height: Int, title: String): Long {
    glfwSetErrorCallback(GLFWErrorCallback.createPrint(System.err))
    if (!glfwInit()) error("Failed to initialise GLFW.")

    glfwWindowHint(GLFW_CLIENT_API, GLFW_OPENGL_API)
    glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE)
    glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE)
    glfwWindowHint(GLFW_DECORATED, GLFW_TRUE)
    glfwWindowHint(GLFW_FOCUSED, GLFW_FALSE)
    glfwWindowHint(GLFW_FLOATING, GLFW_FALSE)
    glfwWindowHint(GLFW_MAXIMIZED, GLFW_FALSE)
    glfwWindowHint(GLFW_SCALE_TO_MONITOR, GLFW_FALSE)
    glfwWindowHint(GLFW_TRANSPARENT_FRAMEBUFFER, GLFW_FALSE)
    glfwWindowHint(GLFW_FOCUS_ON_SHOW, GLFW_TRUE)

    val windowID = glfwCreateWindow(
        width, height, title, MemoryUtil.NULL, MemoryUtil.NULL
    ).also { if (it == MemoryUtil.NULL) error("Failed to create GLFW window.") }

    val monitorID = glfwGetPrimaryMonitor() .takeIf { it != MemoryUtil.NULL } ?: error("Failed to get primary monitor")
    val mode = glfwGetVideoMode(monitorID) ?: error("Failed to get primary monitor video mode")
    val monitorX = IntArray(1)
    val monitorY = IntArray(1)
    val windowWidth = IntArray(1)
    val windowHeight = IntArray(1)

    glfwGetMonitorPos(monitorID, monitorX, monitorY)
    glfwGetWindowSize(windowID, windowWidth, windowHeight)
    glfwSetWindowPos(windowID,
        monitorX[0] + (mode.width() - windowWidth[0]) / 2,
        monitorY[0] + (mode.height() - windowHeight[0]) / 2)
    glfwShowWindow(windowID)

    return windowID
}

/** Create an event stream for window events. */
fun glfwHookEvents(windowID: Long): EventStream<WindowEvent> {
    val subscriptions = mutableListOf<(WindowEvent) -> Unit>()
    val pm = PointerManager()
    val wb = IntArray(1)
    val hb = IntArray(1)

    glfwGetFramebufferSize(windowID, wb, hb)
    var width = wb[0].toFloat()
    var height = hb[0].toFloat()

    fun event(event: WindowEvent) {
        synchronized(subscriptions) {
            subscriptions.forEach { it(event) }
        }
    }

    glfwSetKeyCallback(windowID) { _, key, scancode, action, mods ->
        if (key == GLFW_KEY_V && (mods and GLFW_MOD_CONTROL) != 0) {
            if (action == GLFW_PRESS) glfwGetClipboardString(MemoryUtil.NULL)?.let {
                event(InputEvent(GenericInput.Text(it)))
            }
        }
        else KeyCombination.fromGLFW(key, scancode, mods)?.let {
            event(KeyEvent(
                pressed = action == GLFW_RELEASE,
                held = action == GLFW_PRESS,
                key = it,
            ))
        }
    }

    glfwSetMouseButtonCallback(windowID) { _, button, action, mods ->
        val xb = DoubleArray(1)
        val yb = DoubleArray(1)
        glfwGetCursorPos(windowID, xb, yb)
        val position = ScreenPosition(xb[0].toFloat(), yb[0].toFloat())

        if (action == GLFW_PRESS) {
            pm.press(position, button, mods).forEach(::event)
        }
        else if (action == GLFW_RELEASE) {
            pm.release(position, button, mods).forEach(::event)
        }
    }

    glfwSetCursorPosCallback(windowID) { _, x, y ->
        pm.move(ScreenPosition(x.toFloat(), y.toFloat())).forEach(::event)
    }

    glfwSetScrollCallback(windowID) { _, dx, dy ->
        event(ScrollEvent(dx.toFloat(), dy.toFloat()))
    }

    glfwSetCharCallback(windowID) { _, codepoint ->
        event(InputEvent(GenericInput.Text(String(Character.toChars(codepoint)))))
    }

    glfwSetDropCallback(windowID) { _, count, pathsPtr ->
        val pathsBuffer = MemoryUtil.memPointerBuffer(pathsPtr, count)
        val paths = (0 until count).map { MemoryUtil.memUTF8(MemoryUtil.memByteBufferNT1(pathsBuffer[it])) }
        event(InputEvent(GenericInput.Files(paths)))
    }

    glfwSetWindowRefreshCallback(windowID) {
        event(RedrawEvent(width, height))
    }

    glfwSetFramebufferSizeCallback(windowID) { _, w, h ->
        width = w.toFloat()
        height = h.toFloat()
        event(RedrawEvent(width, height))
    }

    return EventStream { onEvent ->
        synchronized(subscriptions) { subscriptions.add(onEvent) }

        EventStreamConnection {
            synchronized(subscriptions) { subscriptions.remove(onEvent) }
        }
    }
}

/** Close the window. */
fun glfwClose(windowID: Long) {
    glfwSetWindowShouldClose(windowID, true)
}

/** Run an update loop. */
fun glfwLoop(windowID: Long) {
    while (!glfwWindowShouldClose(windowID)) {
        glfwWaitEventsTimeout(0.02)
    }
}

/** Terminate GLFW and destroy the window previously created. */
fun terminateGLFW(windowID: Long) {
    glfwDestroyWindow(windowID)
    glfwTerminate()
    glfwSetErrorCallback(null)?.free()
}

////////////////////////////////////////////////////////////////////////////////

private class PointerManager {
    sealed class State {
        object Init: State()

        data class SinglePress(
            val events: PointerEventStreamImpl,
            val position: ScreenPosition,
            val time: Double,
        ): State()

        data class Pressed(
            val mode: PointerInteractionMode,
            val events: PointerEventStreamImpl,
            val firstPosition: ScreenPosition,
            val moved: Boolean = false,
            val positions: List<ScreenPosition> = listOf(firstPosition),
        ): State()
    }

    fun press(position: ScreenPosition, button: Int, modifiers: Int): List<PointerEvent> {
        return if (button == GLFW_MOUSE_BUTTON_LEFT) {
            val thisState = state

            if (thisState is State.SinglePress && glfwGetTime() <= thisState.time + TIMEOUT && !movedTooFar(thisState.position, position)) {
                state = State.Pressed(PointerInteractionMode.Secondary, thisState.events, thisState.position)
                emptyList()
            }
            else {
                val eventStream = PointerEventStreamImpl()
                state = State.Pressed(PointerInteractionMode.Primary, eventStream, position)
                listOf(PointerEvent(position, eventStream))
            }
        }
        else {
            val eventStream = PointerEventStreamImpl()
            state = State.Pressed(PointerInteractionMode.Alternate, eventStream, position)
            listOf(PointerEvent(position, eventStream))
        }
    }

    fun release(position: ScreenPosition, button: Int, modifiers: Int): List<PointerEvent> {
        (state as? State.Pressed) ?.let { thisState ->
            state = if (thisState.moved) State.Init else {
                thisState.events.push(PointerUpdateEvent.Press(thisState.mode, position))
                when (thisState.mode) {
                    PointerInteractionMode.Primary -> State.SinglePress(thisState.events, position, glfwGetTime())
                    else -> State.Init
                }
            }
        }

        return emptyList()
    }

    fun move(position: ScreenPosition): List<PointerEvent> {
        (state as? State.Pressed) ?.let { thisState ->
            val positions = thisState.positions + position
            state = State.Pressed(thisState.mode, thisState.events, thisState.firstPosition, true, positions)
            thisState.events.push(PointerUpdateEvent.Drag(thisState.mode, thisState.firstPosition, position, positions))
        }

        return emptyList()
    }

    private fun movedTooFar(a: ScreenPosition, b: ScreenPosition) =
        (a.x - b.x) * (a.x - b.x) + (a.y - b.y) * (a.y - b.y) > MOVEMENT_THRESHOLD_SQUARED

    private var state = State.Init as State

    companion object {
        private const val MOVEMENT_THRESHOLD = 10f

        const val TIMEOUT = 0.2
        const val MOVEMENT_THRESHOLD_SQUARED = MOVEMENT_THRESHOLD * MOVEMENT_THRESHOLD
    }
}

private class PointerEventStreamImpl: PointerEventStream {
    override fun <T> connect(initial: T, onUpdate: (T, PointerUpdateEvent) -> T) {
        subs.add(Sub(initial, onUpdate))
    }

    fun push(event: PointerUpdateEvent) {
        for (i in subs.indices) subs[i] = subs[i].update(event)
    }

    private val subs: MutableList<Sub<*>> = mutableListOf()

    private data class Sub<T>(
        val value: T,
        val onUpdate: (T, PointerUpdateEvent) -> T
    ) {
        fun update(event: PointerUpdateEvent) = Sub(onUpdate(value, event), onUpdate)
    }

}
