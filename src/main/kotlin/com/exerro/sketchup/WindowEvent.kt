package com.exerro.sketchup

import org.lwjgl.glfw.GLFW
import java.util.*

sealed class WindowEvent

////////////////////////////////////////////////////////////////////////////////

data class RedrawEvent(
    val width: Float,
    val height: Float
): WindowEvent()

////////////////////////////////////////////////////////////////////////////////

data class PointerDrag(
    /** First position of the drag. */
    val firstPosition: WindowPosition,
    /** Most recent position of the drag. */
    val lastPosition: WindowPosition,
    /** All positions that have been registered during the drag, including first
     *  and last. */
    val allPositions: List<WindowPosition>,
)

/** Represents 3 different kinds of interaction that are possible for different
 *  input modes. */
enum class PointerMode {
    Primary,
    Alternate,
    Secondary
}

/** Fired when a mouse/touch press has occurred. May invalidate a previous press
 *  (e.g. for double taps). */
data class PointerPressEvent(
    val mode: PointerMode,
    val position: WindowPosition,
    val invalidates: WindowEvent?
): WindowEvent()

/** Fired when a mouse/touch press has occurred. May invalidate a previous press
 *  (e.g. for double taps). This event will be fired for every movement of the
 *  cursor/touch and also on its release. */
data class PointerDragEvent(
    val mode: PointerMode,
    val drag: PointerDrag,
    /** Whether this is an ongoing drag (e.g. has not been released). */
    val ongoing: Boolean,
    val invalidates: WindowEvent?
): WindowEvent()

////////////////////////////////////////////////////////////////////////////////

enum class ScrollMode {
    Primary,
    Secondary
}

data class ScrollEvent(
    val mode: ScrollMode,
    val delta: Vector<ScreenSpace>
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

        fun fromName(name: String): KeyCombination {
            val parts = name.split(Regex("[+-]"))
            val (modifiers, name) = parts.dropLast(1).map(String::toLowerCase) to parts.last()
            val ctrl = "ctrl" in modifiers
            val shift = "shift" in modifiers
            val alt = "alt" in modifiers
            val sup = "super" in modifiers
            return KeyCombination(name, ctrl(GLFW.GLFW_MOD_CONTROL) or shift(GLFW.GLFW_MOD_SHIFT) or alt(GLFW.GLFW_MOD_ALT) or sup(GLFW.GLFW_MOD_SUPER))
        }

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
