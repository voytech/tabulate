package io.github.voytech.tabulate.components.table.model.attributes.cell

import io.github.voytech.tabulate.components.table.api.builder.CellAttributeBuilder
import io.github.voytech.tabulate.components.table.api.builder.dsl.CellLevelAttributesBuilderApi
import io.github.voytech.tabulate.components.table.api.builder.dsl.ColumnLevelAttributesBuilderApi
import io.github.voytech.tabulate.components.table.api.builder.dsl.RowLevelAttributesBuilderApi
import io.github.voytech.tabulate.components.table.api.builder.dsl.TableLevelAttributesBuilderApi
import io.github.voytech.tabulate.components.table.model.attributes.cell.enums.DefaultHorizontalAlignment
import io.github.voytech.tabulate.components.table.model.attributes.cell.enums.DefaultVerticalAlignment
import io.github.voytech.tabulate.components.table.model.attributes.cell.enums.contract.HorizontalAlignment
import io.github.voytech.tabulate.components.table.model.attributes.cell.enums.contract.VerticalAlignment
import io.github.voytech.tabulate.core.api.builder.dsl.TabulateMarker

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

fun <T: Any> CellLevelAttributesBuilderApi<T>.alignment(block: CellAlignmentAttribute.Builder.() -> Unit) =
    attribute(CellAlignmentAttribute.Builder().apply(block))

fun <T: Any> ColumnLevelAttributesBuilderApi<T>.alignment(block: CellAlignmentAttribute.Builder.() -> Unit) =
    attribute(CellAlignmentAttribute.Builder().apply(block))

fun <T: Any> RowLevelAttributesBuilderApi<T>.alignment(block: CellAlignmentAttribute.Builder.() -> Unit) =
    attribute(CellAlignmentAttribute.Builder().apply(block))

fun <T: Any> TableLevelAttributesBuilderApi<T>.alignment(block: CellAlignmentAttribute.Builder.() -> Unit) =
    attribute(CellAlignmentAttribute.Builder().apply(block))