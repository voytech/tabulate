package pl.voytech.exporter.core.model.attributes.cell.enums

import pl.voytech.exporter.core.model.attributes.cell.enums.contract.BorderStyle

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