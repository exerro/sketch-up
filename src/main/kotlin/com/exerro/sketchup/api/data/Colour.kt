package com.exerro.sketchup.api.data

import java.util.*
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow

// HSV algorithms from http://marcocorvi.altervista.org/games/imgpr/rgb-hsl.htm

interface Colour {
    val alpha: Float
    val brightness: Float get() = 0.299f * red + 0.587f * green + 0.114f * blue

    val rgb: RGB
    val red: Float get() = rgb.red
    val green: Float get() = rgb.green
    val blue: Float get() = rgb.blue

    val hsl: HSL
    val hue: Float get() = hsl.hue
    val saturation: Float get() = hsl.saturation
    val lightness: Float get() = hsl.lightness

    /** Return a copy of the colour with modified values. Note, this does not
     *  synchronise the values in any way and should therefore be used
     *  carefully. */
    fun copy(
        alpha: Float = this.alpha, brightness: Float = this.brightness,
        rgb: RGB = this.rgb, red: Float = this.red, green: Float = this.green, blue: Float = this.blue,
        hsl: HSL = this.hsl, hue: Float = this.hue, saturation: Float = this.saturation, lightness: Float = this.lightness,
    ) = object: Colour {
        override val alpha = alpha
        override val brightness = brightness
        override val rgb = rgb
        override val red = red
        override val green = green
        override val blue = blue
        override val hsl = hsl
        override val hue = hue
        override val saturation = saturation
        override val lightness = lightness
    }

    /** Return the same colour with a different alpha value. */
    fun withAlpha(alpha: Float) = copy(alpha = alpha)

    /** Return a lighter variant of the colour. */
    fun lighten(amount: Float = 0.1f) =
        hsla(hue, saturation, min(lightness + amount, 1f), alpha)

    /** Return a darker variant of the colour. */
    fun darken(amount: Float = 0.1f) =
        hsla(hue, saturation, max(lightness - amount, 0f), alpha)

    fun identity() = this

    companion object {
        fun rgb(red: Float, green: Float, blue: Float) = rgba(red, green, blue)
        fun rgba(red: Float, green: Float, blue: Float, alpha: Float = 1f) = object: Colour {
            override val alpha = alpha
            override val rgb = RGB(red, green, blue)
            override val red = red
            override val green = green
            override val blue = blue
            override val hsl by lazy {
                val max = max(max(red, green), blue)
                val min = min(min(red, green), blue)
                val l = (max + min) / 2
                val s = when {
                    max == min -> 0f
                    l < 0.5f -> (max - min) / (max + min)
                    else -> (max - min) / (2 - max - min)
                }
                val h = when {
                    max == min -> 0f
                    red == max -> (green - blue) / (max - min)
                    green == max -> 2 + (blue - red) / (max - min)
                    else -> 4 + (red - green) / (max - min)
                } cycle 6f
                HSL(h, s, l)
            }

            override fun equals(other: Any?) = other is Colour && rgb == other.rgb
            override fun hashCode() = Objects.hash(red, green, blue, alpha)
            override fun toString() = "RGB($red, $green, $blue)"
        }

        fun hsl(hue: Float, saturation: Float, lightness: Float) = hsla(hue, saturation, lightness)
        fun hsla(hue: Float, saturation: Float, lightness: Float, alpha: Float = 1f) = object: Colour {
            override val alpha = alpha
            override val rgb by lazy {
                if (saturation == 0f) {
                    RGB(lightness, lightness, lightness)
                }
                else {
                    val t2 = if (lightness < 0.5f) lightness * (1 + saturation) else lightness + saturation - lightness * saturation
                    val t1 = 2 * lightness - t2
                    val h = (hue / 6f) cycle 1f
                    val r = ((h + 1 / 3f) cycle 1f).calculateRGBComponent(t1, t2)
                    val g = h.calculateRGBComponent(t1, t2)
                    val b = ((h - 1 / 3f) cycle 1f).calculateRGBComponent(t1, t2)
                    RGB(r, g, b)
                }
            }
            override val hsl = HSL(hue, saturation, lightness)
            override val hue = hue
            override val saturation = saturation
            override val lightness = lightness

            override fun equals(other: Any?) = other is Colour && hsl == other.hsl
            override fun hashCode() = Objects.hash(red, green, blue, alpha)
            override fun toString() = "HSL($hue, $saturation, $lightness)"
        }

        fun rgba(red: Double, green: Double, blue: Double, alpha: Double = 1.0) = rgba(red.toFloat(), green.toFloat(), blue.toFloat(), alpha.toFloat())
        fun rgb(red: Double, green: Double, blue: Double) = rgb(red.toFloat(), green.toFloat(), blue.toFloat())
        fun hsla(hue: Double, saturation: Double, lightness: Double, alpha: Double = 1.0) = hsla(hue.toFloat(), saturation.toFloat(), lightness.toFloat(), alpha.toFloat())
        fun hsl(hue: Double, saturation: Double, lightness: Double) = hsl(hue.toFloat(), saturation.toFloat(), lightness.toFloat())
        fun greyscale(lightness: Double) = greyscale(lightness.toFloat())

        fun greyscale(lightness: Float) = object: Colour {
            override val alpha = 1f
            override val brightness = lightness
            override val rgb = RGB(lightness, lightness, lightness)
            override val red = lightness
            override val green = lightness
            override val blue = lightness
            override val hsl = HSL(0f, 0f, lightness)
            override val hue = 0f
            override val saturation = 0f
            override val lightness = lightness
        }

        /** Interpolate between colours [a] and [b] without gamma correction.
         *  When [ratio] is 0, [a] will be returned. When [ratio] is 1, [b] will
         *  be returned. [ratio] values between 0 and 1 will mix gradually from
         *  [a] to [b]. */
        fun mixRGBNoGamma(ratio: Float, a: Colour, b: Colour) = rgba(
            red = a.red * (1 - ratio) + b.red * ratio,
            green = a.green * (1 - ratio) + b.green * ratio,
            blue = a.blue * (1 - ratio) + b.blue * ratio,
            alpha = a.alpha * (1 - ratio) + b.alpha * ratio
        )

        /** Interpolate between colours [a] and [b] doing gamma correction. When
         *  [ratio] is 0, [a] will be returned. When [ratio] is 1, [b] will be
         *  returned. [ratio] values between 0 and 1 will mix gradually from [a]
         *  to [b]. */
        fun mixRGB(ratio: Float, a: Colour, b: Colour, gamma: Float = 2.2f): Colour {
            val rgba = rgba(
                red = (a.red.pow(gamma) * (1 - ratio) + b.red.pow(gamma) * ratio).pow(1 / gamma),
                green = (a.green.pow(gamma) * (1 - ratio) + b.green.pow(gamma) * ratio).pow(1 / gamma),
                blue = (a.blue.pow(gamma) * (1 - ratio) + b.blue.pow(gamma) * ratio).pow(1 / gamma),
                alpha = a.alpha * (1 - ratio) + b.alpha * ratio
            )
            return rgba
        }

        fun mixHSL(ratio: Float, a: Colour, b: Colour): Colour = hsla(
            hue = when {
                a.hue < b.hue && b.hue - a.hue > 3f -> (a.hue * (1 - ratio) + (b.hue - 6) * ratio) cycle 6f
                a.hue > b.hue && a.hue - b.hue > 3f -> ((a.hue - 6) * (1 - ratio) + (b.hue) * ratio) cycle 6f
                else -> a.hue * (1 - ratio) + b.hue * ratio
            },
            saturation = a.saturation * (1 - ratio) + b.saturation * ratio,
            lightness = a.lightness * (1 - ratio) + b.lightness * ratio,
            alpha = a.alpha * (1 - ratio) + b.alpha * ratio
        )

        val red = rgb(0.8, 0.3, 0.3)
        val green = rgb(0.2, 0.8, 0.4)
        val blue = rgb(0.1, 0.5, 0.9)
        val yellow = rgb(0.9, 0.85, 0.3)
        val orange = rgb(0.9, 0.5, 0.3)
        val pink = rgb(0.9, 0.4, 0.6)
        val purple = rgb(0.7, 0.1, 1.0)
        val cyan = rgb(0.2, 0.7, 0.7)

        val white = greyscale(0.95)
        val ultraLightGrey = greyscale(0.85)
        val lightGrey = greyscale(0.75)
        val lighterGrey = rgb(0.59, 0.58, 0.6)
        val grey = rgb(0.33, 0.33, 0.35)
        val darkGrey = rgb(0.19, 0.19, 0.2)
        val charcoal = rgb(0.13, 0.13, 0.14)
        val black = greyscale(0.1)
    }
}

data class RGB(
    val red: Float,
    val green: Float,
    val blue: Float
)

data class HSL(
    val hue: Float,
    val saturation: Float,
    val lightness: Float
)

private fun Float.calculateRGBComponent(temp1: Float, temp2: Float) = when {
    this < 1/6f -> temp1 + (temp2 - temp1) * 6 * this
    this < 1/2f -> temp2
    this < 2/3f -> temp1 + (temp2 - temp1) * (2/3f - this) * 6
    else -> temp1
}

private infix fun Float.cycle(d: Float): Float {
    var n = this % d
    while (n < 0f) n += d
    while (n >= d) n -= d
    return n
}
