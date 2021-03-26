package pl.voytech.exporter.core.model.attributes.style.enums

import pl.voytech.exporter.core.model.attributes.style.enums.contract.BorderStyle

enum class BaseBorderStyle : BorderStyle {
    DASHED,
    SOLID,
    DOTTED,
    NONE,
    DOUBLE,
    INSET,
    OUTSET,
    GROOVE;

    override fun getAttributeId(): String = this.name
}