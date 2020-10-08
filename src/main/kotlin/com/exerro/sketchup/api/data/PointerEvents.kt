package com.exerro.sketchup.api.data

import com.exerro.sketchup.api.Event

/** Fired when a mouse/touch press has occurred. */
data class PointerPressEvent(
    val mode: PointerMode,
    val point: Point<ScreenSpace>
): Event

/** Fired when a mouse/touch press has occurred. May invalidate a previous press
 *  (e.g. for double taps). This event will be fired for every movement of the
 *  cursor/touch and also on its release. */
data class PointerDragEvent(
    val mode: PointerMode,
    val path: Path<ScreenSpace>,
    /** True if the pointer has been released. */
    val complete: Boolean
): Event

data class PointEvent(
    val type: InteractionType,
    val point: Point<ScreenSpace>
): Event

data class PartialPathEvent(
    val type: InteractionType,
    val path: Path<ScreenSpace>,
): Event

data class CompletePathEvent(
    val type: InteractionType,
    val path: Path<ScreenSpace>,
): Event
