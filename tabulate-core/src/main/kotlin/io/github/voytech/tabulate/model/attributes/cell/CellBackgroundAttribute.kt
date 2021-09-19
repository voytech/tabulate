package io.github.voytech.tabulate.model.attributes.cell

import io.github.voytech.tabulate.api.builder.CellAttributeBuilder
import io.github.voytech.tabulate.api.builder.dsl.*
import io.github.voytech.tabulate.model.attributes.cell.enums.contract.CellFill

data class CellBackgroundAttribute(
    val color: Color? = null,
    val fill: CellFill? = null
) : CellStyleAttribute<CellBackgroundAttribute>() {

    @TabulateMarker
    class Builder: CellAttributeBuilder<CellBackgroundAttribute>() {
        var color: Color? by observable(null)
        var fill: CellFill? by observable(null)
        override fun provide(): CellBackgroundAttribute = CellBackgroundAttribute(color, fill)
    }

    override fun overrideWith(other: CellBackgroundAttribute): CellBackgroundAttribute = CellBackgroundAttribute(
        color = takeIfChanged(other, CellBackgroundAttribute::color),
        fill = takeIfChanged(other, CellBackgroundAttribute::fill),
    )
}

fun <T> CellLevelAttributesBuilderApi<T>.background(block: CellBackgroundAttribute.Builder.() -> Unit) =
    attribute(CellBackgroundAttribute.Builder().apply(block).build())

fun <T> ColumnLevelAttributesBuilderApi<T>.background(block: CellBackgroundAttribute.Builder.() -> Unit) =
    attribute(CellBackgroundAttribute.Builder().apply(block).build())

fun <T> RowLevelAttributesBuilderApi<T>.background(block: CellBackgroundAttribute.Builder.() -> Unit) =
    attribute(CellBackgroundAttribute.Builder().apply(block).build())

fun <T> TableLevelAttributesBuilderApi<T>.background(block: CellBackgroundAttribute.Builder.() -> Unit) =
    attribute(CellBackgroundAttribute.Builder().apply(block).build())