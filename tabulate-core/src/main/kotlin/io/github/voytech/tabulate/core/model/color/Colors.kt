package io.github.voytech.tabulate.core.model.color

object Colors {
    @JvmField
    val RED = Color(r = 255, g = 0, b = 0)

    @JvmField
    val GREEN = Color(r = 0, g = 255, b = 0)

    @JvmField
    val BLUE = Color(r = 0, g = 0, b = 255)

    @JvmField
    val YELLOW = Color(r = 255, g = 255, b = 0)

    @JvmField
    val BLACK = Color(r = 0, g = 0, b = 0)

    @JvmField
    val LIGHT_GRAY = Color(r = 211, g = 211, b = 211)

    @JvmField
    val WHITE = Color(r = 255, g = 255, b = 255)

    @JvmField
    val ORANGE = Color(r = 255, g = 165, b = 0)

    @JvmField
    val PINK = Color(r = 255, g = 20, b = 147)

    @JvmField
    val AERO = Color(r = 124, g = 185, b = 232)

    @JvmField
    val AERO_BLUE = Color(r = 201, g = 255, b = 229)

    @JvmField
    val AMBER = Color(r = 255, g = 191, b = 0)

    @JvmField
    val AMARANTH = Color(r = 229, g = 43, b = 80)

}

fun Color.darken(amount: Float): Color = copy(
    r = r.div(amount).toInt(),
    g = g.div(amount).toInt(),
    b = b.div(amount).toInt()
)

fun Color.lighten(amount: Float): Color = copy(
    r = (r*amount).toInt().coerceAtMost(255),
    g = (g*amount).toInt().coerceAtMost(255),
    b = (b*amount).toInt().coerceAtMost(255)
)