package pl.voytech.exporter.core.model.attributes.style

import pl.voytech.exporter.core.api.builder.CellAttributeBuilder
import pl.voytech.exporter.core.model.attributes.style.enums.HorizontalAlignment
import pl.voytech.exporter.core.model.attributes.style.enums.VerticalAlignment

data class CellAlignmentAttribute(
    val vertical: VerticalAlignment? = null,
    val horizontal: HorizontalAlignment? = null
) : CellStyleAttribute<CellAlignmentAttribute>() {
    class Builder : CellAttributeBuilder<CellAlignmentAttribute> {
        var vertical: VerticalAlignment? = null
        var horizontal: HorizontalAlignment? = null
        override fun build(): CellAlignmentAttribute = CellAlignmentAttribute(vertical, horizontal)
    }

    override fun mergeWith(other: CellAlignmentAttribute): CellAlignmentAttribute = CellAlignmentAttribute(
        vertical = other.vertical ?: this.vertical,
        horizontal = other.horizontal ?: this.horizontal
    )

}

fun alignment(block: CellAlignmentAttribute.Builder.() -> Unit): CellAlignmentAttribute =
    CellAlignmentAttribute.Builder().apply(block).build()
