package io.github.voytech.tabulate.core.model.attributes.cell

import io.github.voytech.tabulate.core.api.builder.CellAttributeBuilder
import io.github.voytech.tabulate.core.model.attributes.cell.enums.contract.CellFill

data class CellBackgroundAttribute(
    val color: Color? = null,
    val fill: CellFill? = null
) : CellStyleAttribute<CellBackgroundAttribute>() {

    class Builder: CellAttributeBuilder<CellBackgroundAttribute> {
        var color: Color? = null
        var fill: CellFill? = null
        override fun build(): CellBackgroundAttribute = CellBackgroundAttribute(color, fill)
    }

    override fun mergeWith(other: CellBackgroundAttribute): CellBackgroundAttribute = CellBackgroundAttribute(
        color = other.color ?: this.color,
        fill = other.fill ?: this.fill
    )
}

fun background(block: CellBackgroundAttribute.Builder.() -> Unit): CellBackgroundAttribute =
    CellBackgroundAttribute.Builder().apply(block).build()
