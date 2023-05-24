package io.github.voytech.tabulate.core.model.color

import io.github.voytech.tabulate.core.api.builder.dsl.DSLCommand

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

interface DefaultColorWords {
    var color: Color?
    val red : DSLCommand
        get() {
            color = Colors.RED; return DSLCommand
        }

    val green : DSLCommand
        get() {
            color = Colors.GREEN; return DSLCommand
        }

    val blue : DSLCommand
        get() {
            color = Colors.BLUE; return DSLCommand
        }

    val yellow : DSLCommand
        get() {
            color = Colors.YELLOW; return DSLCommand
        }

    val black : DSLCommand
        get() {
            color = Colors.BLACK; return DSLCommand
        }

    val lightGray : DSLCommand
        get() {
            color = Colors.LIGHT_GRAY; return DSLCommand
        }

    val white : DSLCommand
        get() {
            color = Colors.WHITE; return DSLCommand
        }

    val orange : DSLCommand
        get() {
            color = Colors.ORANGE; return DSLCommand
        }

    val pink : DSLCommand
        get() {
            color = Colors.PINK; return DSLCommand
        }

    val aero : DSLCommand
        get() {
            color = Colors.AERO; return DSLCommand
        }

    val aeroBlue : DSLCommand
        get() {
            color = Colors.AERO_BLUE; return DSLCommand
        }

    val amber : DSLCommand
        get() {
            color = Colors.AMBER; return DSLCommand
        }

    val amaranth : DSLCommand
        get() {
            color = Colors.AMARANTH; return DSLCommand
        }

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


