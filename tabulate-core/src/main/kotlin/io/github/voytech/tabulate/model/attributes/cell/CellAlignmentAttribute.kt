package io.github.voytech.tabulate.model.attributes.cell

import io.github.voytech.tabulate.api.builder.CellAttributeBuilder
import io.github.voytech.tabulate.api.builder.dsl.*
import io.github.voytech.tabulate.model.attributes.cell.enums.DefaultHorizontalAlignment
import io.github.voytech.tabulate.model.attributes.cell.enums.DefaultVerticalAlignment
import io.github.voytech.tabulate.model.attributes.cell.enums.contract.HorizontalAlignment
import io.github.voytech.tabulate.model.attributes.cell.enums.contract.VerticalAlignment

data class CellAlignmentAttribute(
    val vertical: VerticalAlignment? = DefaultVerticalAlignment.BOTTOM,
    val horizontal: HorizontalAlignment? = DefaultHorizontalAlignment.LEFT
) : CellStyleAttribute<CellAlignmentAttribute>() {

    @TabulateMarker
    class Builder : CellAttributeBuilder<CellAlignmentAttribute>() {
        var vertical: VerticalAlignment? by observable(DefaultVerticalAlignment.BOTTOM)
        var horizontal: HorizontalAlignment? by observable(DefaultHorizontalAlignment.LEFT)
        override fun provide(): CellAlignmentAttribute = CellAlignmentAttribute(vertical, horizontal)
    }

    override fun overrideWith(other: CellAlignmentAttribute): CellAlignmentAttribute = CellAlignmentAttribute(
        vertical = takeIfChanged(other, CellAlignmentAttribute::vertical),
        horizontal = takeIfChanged(other, CellAlignmentAttribute::horizontal),
    )

    companion object {
        @JvmStatic
        fun builder() : Builder = Builder()
    }

}

fun <T> CellLevelAttributesBuilderApi<T>.alignment(block: CellAlignmentAttribute.Builder.() -> Unit) =
    attribute(CellAlignmentAttribute.Builder().apply(block))

fun <T> ColumnLevelAttributesBuilderApi<T>.alignment(block: CellAlignmentAttribute.Builder.() -> Unit) =
    attribute(CellAlignmentAttribute.Builder().apply(block))

fun <T> RowLevelAttributesBuilderApi<T>.alignment(block: CellAlignmentAttribute.Builder.() -> Unit) =
    attribute(CellAlignmentAttribute.Builder().apply(block))

fun <T> TableLevelAttributesBuilderApi<T>.alignment(block: CellAlignmentAttribute.Builder.() -> Unit) =
    attribute(CellAlignmentAttribute.Builder().apply(block))