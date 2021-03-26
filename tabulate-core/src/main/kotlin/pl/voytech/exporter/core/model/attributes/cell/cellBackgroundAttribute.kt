package pl.voytech.exporter.core.model.attributes.cell

import pl.voytech.exporter.core.api.builder.CellAttributeBuilder
import pl.voytech.exporter.core.model.attributes.cell.enums.DefaultCellFill
import pl.voytech.exporter.core.model.attributes.cell.enums.contract.CellFill

data class CellBackgroundAttribute(
    val color: Color,
    val fill: CellFill? = DefaultCellFill.SOLID
) : CellStyleAttribute<CellBackgroundAttribute>() {

    class Builder: CellAttributeBuilder<CellBackgroundAttribute> {
        lateinit var color: Color
        var fill: CellFill? = DefaultCellFill.SOLID
        override fun build(): CellBackgroundAttribute = CellBackgroundAttribute(color, fill)
    }

    override fun mergeWith(other: CellBackgroundAttribute): CellBackgroundAttribute = other.copy(color = other.color, fill = other.fill ?: this.fill)
}

fun background(block: CellBackgroundAttribute.Builder.() -> Unit): CellBackgroundAttribute =
    CellBackgroundAttribute.Builder().apply(block).build()
