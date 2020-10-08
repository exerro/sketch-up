package com.exerro.sketchup.api.data

import org.lwjgl.glfw.GLFW
import java.util.*

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
            return KeyCombination(
                name,
                ctrl(GLFW.GLFW_MOD_CONTROL) or shift(GLFW.GLFW_MOD_SHIFT) or alt(GLFW.GLFW_MOD_ALT) or sup(GLFW.GLFW_MOD_SUPER)
            )
        }

        @Suppress("NOTHING_TO_INLINE")
        private inline operator fun Boolean.invoke(modifier: Int) = if (this) modifier else 0
    }
}