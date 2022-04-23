package io.github.voytech.tabulate.components.table.model.attributes.cell

import io.github.voytech.tabulate.components.table.api.builder.CellAttributeBuilder
import io.github.voytech.tabulate.components.table.api.builder.dsl.CellLevelAttributesBuilderApi
import io.github.voytech.tabulate.components.table.api.builder.dsl.ColumnLevelAttributesBuilderApi
import io.github.voytech.tabulate.components.table.api.builder.dsl.RowLevelAttributesBuilderApi
import io.github.voytech.tabulate.components.table.api.builder.dsl.TableLevelAttributesBuilderApi
import io.github.voytech.tabulate.components.table.model.attributes.Color
import io.github.voytech.tabulate.components.table.model.attributes.cell.enums.DefaultCellFill
import io.github.voytech.tabulate.components.table.model.attributes.cell.enums.contract.CellFill
import io.github.voytech.tabulate.core.api.builder.dsl.TabulateMarker

data class CellBackgroundAttribute(
    val color: Color? = null,
    val fill: CellFill = DefaultCellFill.SOLID
) : CellStyleAttribute<CellBackgroundAttribute>() {

    @TabulateMarker
    class Builder: CellAttributeBuilder<CellBackgroundAttribute>() {
        var color: Color? by observable(null)
        var fill: CellFill by observable(DefaultCellFill.SOLID)
        override fun provide(): CellBackgroundAttribute = CellBackgroundAttribute(color, fill)
    }

    override fun overrideWith(other: CellBackgroundAttribute): CellBackgroundAttribute = CellBackgroundAttribute(
        color = takeIfChanged(other, CellBackgroundAttribute::color),
        fill = takeIfChanged(other, CellBackgroundAttribute::fill),
    )

    companion object {
        @JvmStatic
        fun builder() : Builder = Builder()
    }
}

fun <T> CellLevelAttributesBuilderApi<T>.background(block: CellBackgroundAttribute.Builder.() -> Unit) =
    attribute(CellBackgroundAttribute.Builder().apply(block))

fun <T> ColumnLevelAttributesBuilderApi<T>.background(block: CellBackgroundAttribute.Builder.() -> Unit) =
    attribute(CellBackgroundAttribute.Builder().apply(block))

fun <T> RowLevelAttributesBuilderApi<T>.background(block: CellBackgroundAttribute.Builder.() -> Unit) =
    attribute(CellBackgroundAttribute.Builder().apply(block))

fun <T> TableLevelAttributesBuilderApi<T>.background(block: CellBackgroundAttribute.Builder.() -> Unit) =
    attribute(CellBackgroundAttribute.Builder().apply(block))