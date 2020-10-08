package com.exerro.sketchup.api.util

import com.exerro.sketchup.api.DrawContext
import com.exerro.sketchup.api.VisualHint
import com.exerro.sketchup.api.data.BoundingArea
import com.exerro.sketchup.api.data.Colour
import com.exerro.sketchup.api.data.Path
import com.exerro.sketchup.api.data.ScreenSpace

val VisualHint.Companion.none: VisualHint get() = NoHint

fun VisualHint.Companion.path(path: Path<ScreenSpace>) = VisualHint {
    path(path, Colour.lightGrey)
}

fun VisualHint.Companion.rectangularSelection(bounds: BoundingArea<ScreenSpace>) = VisualHint {
    boxOutline(bounds, Colour.white)
}

fun VisualHint.Companion.all(vararg hints: VisualHint) = when (hints.size) {
    0 -> NoHint
    1 -> hints[0]
    else -> UnionHint(hints.toList())
}

////////////////////////////////////////////////////////////////////////////////

operator fun VisualHint.plus(other: VisualHint) = when {
    this == NoHint -> other
    other == NoHint -> this
    this is UnionHint && other !is UnionHint -> UnionHint(hints + other)
    this !is UnionHint && other is UnionHint -> UnionHint(listOf(this) + other.hints)
    this is UnionHint && other is UnionHint -> UnionHint(hints + other.hints)
    else -> UnionHint(listOf(this, other))
}

operator fun VisualHint.minus(other: VisualHint) = when {
    other == NoHint -> this
    this is UnionHint && other !is UnionHint -> UnionHint(hints - other)
    this !is UnionHint && other is UnionHint -> if (this in other.hints) NoHint else this
    this is UnionHint && other is UnionHint -> UnionHint(hints - other.hints)
    else -> this
}

////////////////////////////////////////////////////////////////////////////////

private object NoHint: VisualHint {
    override fun DrawContext.draw() { /* do nothing */ }
}

private class UnionHint(val hints: List<VisualHint>): VisualHint {
    override fun DrawContext.draw() {
        hints.forEach { it.run { draw() } }
    }
}
