package com.exerro.sketchup.api.data

import com.exerro.sketchup.api.Event

sealed class ExtendedWindowEvent: Event
sealed class WindowEvent: ExtendedWindowEvent()

////////////////////////////////////////////////////////////////////////////////

data class PointerPressedEvent(
    val button: PointerButton,
    val modifiers: Set<PointerModifier>,
    val position: Vector<ScreenSpace>,
    val pressure: Scalar<ScreenSpace>,
): WindowEvent()

object PointerReleasedEvent: WindowEvent()

////////////////////////////////////////////////////////////////////////////////

/** An indication to redraw with the given size. */
data class RedrawEvent(
    val windowSize: Vector<ScreenSpace>
): WindowEvent()

////////////////////////////////////////////////////////////////////////////////

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
    val delta: Vector<ScreenSpace>,
    val position: Vector<ScreenSpace>,
): WindowEvent()

////////////////////////////////////////////////////////////////////////////////

data class KeyEvent(
    val pressed: Boolean,
    val held: Boolean,
    val key: KeyCombination
): WindowEvent()

////////////////////////////////////////////////////////////////////////////////

data class InputEvent(
    val input: GenericInput
): WindowEvent()

sealed class GenericInput {
    data class Text(val text: String): GenericInput()
    data class Files(val files: List<String>): GenericInput()
}
