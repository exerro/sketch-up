import org.lwjgl.glfw.GLFW
import java.util.*

sealed class WindowEvent

////////////////////////////////////////////////////////////////////////////////

data class RedrawEvent(
    val width: Float,
    val height: Float
): WindowEvent()

////////////////////////////////////////////////////////////////////////////////

/** Fired when a mouse/touch interaction has started. */
data class PointerEvent(
    val position: ScreenPosition,
    val updates: PointerEventStream,
): WindowEvent()

enum class PointerInteractionMode {
    Primary,
    Alternate,
    Secondary
}

sealed class PointerUpdateEvent {
    data class Press(
        val mode: PointerInteractionMode,
        val position: ScreenPosition,
    ): PointerUpdateEvent()

    data class Drag(
        val mode: PointerInteractionMode,
        val initialPosition: ScreenPosition,
        val finalPosition: ScreenPosition,
        val positions: List<ScreenPosition>,
    ): PointerUpdateEvent()
}

interface PointerEventStream {
    fun <T> connect(initial: T, onUpdate: (T, PointerUpdateEvent) -> T)
}

////////////////////////////////////////////////////////////////////////////////

data class ScrollEvent(
    val dx: Float,
    val dy: Float
): WindowEvent()

////////////////////////////////////////////////////////////////////////////////

data class KeyEvent(
    val pressed: Boolean,
    val held: Boolean,
    val key: KeyCombination
): WindowEvent()

class KeyCombination private constructor(
    private val name: String,
    private val modifiers: Int,
) {
    override fun equals(other: Any?) =
        other is KeyCombination && other.name == name && other.modifiers == modifiers

    override fun hashCode() =
        Objects.hash(name, modifiers)

    override fun toString(): String {
        val ctrl = when (modifiers and GLFW.GLFW_MOD_CONTROL) { 0 -> ""; else -> "ctrl+" }
        val alt = when (modifiers and GLFW.GLFW_MOD_ALT) { 0 -> ""; else -> "alt+" }
        val shift = when (modifiers and GLFW.GLFW_MOD_SHIFT) { 0 -> ""; else -> "shift+" }
        val sup = when (modifiers and GLFW.GLFW_MOD_SUPER) { 0 -> ""; else -> "super+" }
        return "$ctrl$alt$shift$sup$name"
    }

    companion object {
        fun fromGLFW(key: Int, scancode: Int, modifiers: Int) = when (key) {
            GLFW.GLFW_KEY_ENTER -> "enter"
            GLFW.GLFW_KEY_TAB -> "tab"
            GLFW.GLFW_KEY_SPACE -> "space"
            GLFW.GLFW_KEY_BACKSPACE -> "backspace"
            else -> GLFW.glfwGetKeyName(key, scancode)
        } ?.let { name -> KeyCombination(name, modifiers) }

        fun fromName(
            name: String,
            ctrl: Boolean = false,
            shift: Boolean = false,
            alt: Boolean = false,
            sup: Boolean = false
        ) = KeyCombination(name, ctrl(GLFW.GLFW_MOD_CONTROL) or shift(GLFW.GLFW_MOD_SHIFT) or alt(GLFW.GLFW_MOD_ALT) or sup(GLFW.GLFW_MOD_SUPER))

        @Suppress("NOTHING_TO_INLINE")
        private inline operator fun Boolean.invoke(modifier: Int) = if (this) modifier else 0
    }
}

////////////////////////////////////////////////////////////////////////////////

data class InputEvent(
    val input: GenericInput
): WindowEvent()

sealed class GenericInput {
    data class Text(val text: String): GenericInput()
    data class Files(val files: List<String>): GenericInput()
}
