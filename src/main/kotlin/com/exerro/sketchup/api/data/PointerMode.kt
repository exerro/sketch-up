package com.exerro.sketchup.api.data

data class PointerMode(
    val modifiers: Set<PointerModifier>,
    val button: PointerButton,
    val alternate: Boolean,
)

enum class PointerButton {
    Primary,
    Tertiary,
    Secondary
}

enum class PointerModifier {
    Control,
    Shift,
    Alt,
    Super
}
