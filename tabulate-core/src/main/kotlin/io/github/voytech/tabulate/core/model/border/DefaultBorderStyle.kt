package io.github.voytech.tabulate.core.model.border

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