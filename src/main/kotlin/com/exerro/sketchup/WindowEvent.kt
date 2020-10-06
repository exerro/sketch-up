package com.exerro.sketchup

import com.exerro.sketchup.data.*
import com.exerro.sketchup.data.Vector
import org.lwjgl.glfw.GLFW
import java.util.*

sealed class ExtendedWindowEvent
sealed class WindowEvent: ExtendedWindowEvent()

////////////////////////////////////////////////////////////////////////////////

data class PointerPressedEvent(
    val mode: PointerMode,
    val position: Vector<ScreenSpace>,
    val pressure: Scalar<ScreenSpace>,
): ExtendedWindowEvent()

object PointerReleasedEvent: ExtendedWindowEvent()

////////////////////////////////////////////////////////////////////////////////

/** An indication to redraw with the given size. */
data class RedrawEvent(
    val windowSize: WindowSize
): WindowEvent()

////////////////////////////////////////////////////////////////////////////////

/** Represents 3 different kinds of interaction that are possible for different
 *  input modes. */
enum class PointerMode {
    Primary,
    Tertiary,
    Secondary
}

/** Fired when a mouse/touch press has occurred. */
data class PointerPressEvent(
    val mode: PointerMode,
    val alternate: Boolean,
    val point: Point<ScreenSpace>
): WindowEvent()

/** Fired when a mouse/touch press has occurred. May invalidate a previous press
 *  (e.g. for double taps). This event will be fired for every movement of the
 *  cursor/touch and also on its release. */
data class PointerDragEvent(
    val mode: PointerMode,
    val alternate: Boolean,
    val path: Path<ScreenSpace>,
    /** True if the pointer has been released. */
    val complete: Boolean
): WindowEvent()

data class PointerMoveEvent(
    val position: Vector<ScreenSpace>,
    val pressure: Scalar<ScreenSpace>,
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
