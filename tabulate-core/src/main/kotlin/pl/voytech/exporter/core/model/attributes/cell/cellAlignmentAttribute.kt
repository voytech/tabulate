package pl.voytech.exporter.core.model.attributes.cell

import pl.voytech.exporter.core.api.builder.CellAttributeBuilder
import pl.voytech.exporter.core.model.attributes.cell.enums.DefaultHorizontalAlignment
import pl.voytech.exporter.core.model.attributes.cell.enums.DefaultVerticalAlignment
import pl.voytech.exporter.core.model.attributes.cell.enums.contract.HorizontalAlignment
import pl.voytech.exporter.core.model.attributes.cell.enums.contract.VerticalAlignment

data class CellAlignmentAttribute(
    val vertical: VerticalAlignment? = DefaultVerticalAlignment.BOTTOM,
    val horizontal: HorizontalAlignment? = DefaultHorizontalAlignment.LEFT
) : CellStyleAttribute<CellAlignmentAttribute>() {
    class Builder : CellAttributeBuilder<CellAlignmentAttribute> {
        var vertical: VerticalAlignment? = DefaultVerticalAlignment.BOTTOM
        var horizontal: HorizontalAlignment? = DefaultHorizontalAlignment.LEFT
        override fun build(): CellAlignmentAttribute = CellAlignmentAttribute(vertical, horizontal)
    }

    override fun mergeWith(other: CellAlignmentAttribute): CellAlignmentAttribute = CellAlignmentAttribute(
        vertical = other.vertical ?: this.vertical,
        horizontal = other.horizontal ?: this.horizontal
    )

}

fun alignment(block: CellAlignmentAttribute.Builder.() -> Unit): CellAlignmentAttribute =
    CellAlignmentAttribute.Builder().apply(block).build()
