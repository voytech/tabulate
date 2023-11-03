package io.github.voytech.tabulate.core.model.border

import io.github.voytech.tabulate.core.api.builder.dsl.DSLCommand

enum class DefaultBorderStyle : BorderStyle {
    DASHED,
    SOLID,
    DOTTED,
    NONE,
    DOUBLE,
    INSET,
    OUTSET,
    GROOVE;
    override fun getBorderStyleId() = name
}

interface BorderStyleBuilder {
    var style: BorderStyle
}

interface DefaultBorderStyleWords : BorderStyleBuilder {

    val solid: DSLCommand
        get() {
            style = DefaultBorderStyle.SOLID; return DSLCommand
        }
    val dotted: DSLCommand
        get() {
            style = DefaultBorderStyle.DOTTED; return DSLCommand
        }

    val dashed: DSLCommand
        get() {
            style = DefaultBorderStyle.DASHED; return DSLCommand
        }

    val double: DSLCommand
        get() {
            style = DefaultBorderStyle.DOUBLE; return DSLCommand
        }

    val groove: DSLCommand
        get() {
            style = DefaultBorderStyle.GROOVE; return DSLCommand
        }

    val none: DSLCommand
        get() {
            style = DefaultBorderStyle.NONE; return DSLCommand
        }

    val inset: DSLCommand
        get() {
            style = DefaultBorderStyle.INSET; return DSLCommand
        }

    val outset: DSLCommand
        get() {
            style = DefaultBorderStyle.OUTSET; return DSLCommand
        }
}

