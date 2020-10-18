package pl.voytech.exporter.core.model.attributes.style

import pl.voytech.exporter.core.api.builder.CellAttributeBuilder
import pl.voytech.exporter.core.model.attributes.CellAttribute
import pl.voytech.exporter.core.model.attributes.style.enums.HorizontalAlignment
import pl.voytech.exporter.core.model.attributes.style.enums.VerticalAlignment

data class CellAlignmentAttribute(
    val vertical: VerticalAlignment? = null,
    val horizontal: HorizontalAlignment? = null
) : CellStyleAttribute() {
    class Builder : CellAttributeBuilder {
        var vertical: VerticalAlignment? = null
        var horizontal: HorizontalAlignment? = null
        override fun build(): CellAttribute = CellAlignmentAttribute(vertical, horizontal)
    }

    override fun mergeWith(other: CellAttribute): CellAttribute = CellAlignmentAttribute(
        vertical = if (other is CellAlignmentAttribute) other.vertical ?: this.vertical else this.vertical,
        horizontal = if (other is CellAlignmentAttribute) other.horizontal ?: this.horizontal else this.horizontal
    )

}

fun alignment(block: CellAlignmentAttribute.Builder.() -> Unit): CellAttribute =
    CellAlignmentAttribute.Builder().apply(block).build()
