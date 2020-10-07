package com.exerro.sketchup

import com.exerro.sketchup.data.BoundingArea
import com.exerro.sketchup.data.Colour
import com.exerro.sketchup.data.Path
import com.exerro.sketchup.data.ScreenSpace

fun interface VisualHint {
    fun DrawContext.draw()

    companion object {
        val none: VisualHint = NoHint

        fun path(path: Path<ScreenSpace>) = VisualHint {
            path(path, Colour.lightGrey)
        }

        fun rectangularSelection(bounds: BoundingArea<ScreenSpace>) = VisualHint {
            boxOutline(bounds, Colour.white)
        }

        fun all(vararg hints: VisualHint) = when (hints.size) {
            0 -> NoHint
            1 -> hints[0]
            else -> UnionHint(hints.toList())
        }
    }
}

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

private object NoHint: VisualHint {
    override fun DrawContext.draw() { /* do nothing */ }
}

private class UnionHint(
    val hints: List<VisualHint>
): VisualHint {
    override fun DrawContext.draw() {
        hints.forEach { it.run { draw() } }
    }
}
