package com.exerro.sketchup.api.data

import com.exerro.sketchup.api.Event

sealed class ExtendedWindowEvent: Event
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
    val windowSize: Vector<ScreenSpace>
): WindowEvent()

////////////////////////////////////////////////////////////////////////////////

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

////////////////////////////////////////////////////////////////////////////////

data class InputEvent(
    val input: GenericInput
): WindowEvent()

sealed class GenericInput {
    data class Text(val text: String): GenericInput()
    data class Files(val files: List<String>): GenericInput()
}
