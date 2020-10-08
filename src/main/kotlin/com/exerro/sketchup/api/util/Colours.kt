package com.exerro.sketchup.api.util

import com.exerro.sketchup.api.data.Colour

/** Collections of colours. */
object Colours {
    /** All the white, grey and black colours ordered lightest to darkest. */
    val greyscale = listOf(
        Colour.white,
        Colour.ultraLightGrey,
        Colour.lightGrey,
        Colour.lighterGrey,
        Colour.grey,
        Colour.darkGrey,
        Colour.charcoal,
        Colour.black
    )

    /** All the pre-defined colours. */
    val all = greyscale + listOf(
        Colour.red,
        Colour.orange,
        Colour.yellow,
        Colour.green,
        Colour.cyan,
        Colour.blue,
        Colour.purple,
        Colour.pink
    )
}
