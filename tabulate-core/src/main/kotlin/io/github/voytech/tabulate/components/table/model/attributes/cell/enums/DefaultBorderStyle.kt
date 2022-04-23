package io.github.voytech.tabulate.components.table.model.attributes.cell.enums

import io.github.voytech.tabulate.components.table.model.attributes.cell.enums.contract.BorderStyle

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