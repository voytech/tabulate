package io.github.voytech.tabulate.core.model.attributes.cell

import io.github.voytech.tabulate.core.api.builder.CellAttributeBuilder
import io.github.voytech.tabulate.core.model.attributes.cell.enums.DefaultHorizontalAlignment
import io.github.voytech.tabulate.core.model.attributes.cell.enums.DefaultVerticalAlignment
import io.github.voytech.tabulate.core.model.attributes.cell.enums.contract.HorizontalAlignment
import io.github.voytech.tabulate.core.model.attributes.cell.enums.contract.VerticalAlignment

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
