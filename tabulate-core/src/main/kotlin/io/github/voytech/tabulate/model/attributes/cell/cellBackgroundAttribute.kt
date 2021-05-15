package io.github.voytech.tabulate.model.attributes.cell

import io.github.voytech.tabulate.api.builder.CellAttributeBuilder
import io.github.voytech.tabulate.api.builder.dsl.CellLevelAttributesBuilderApi
import io.github.voytech.tabulate.api.builder.dsl.ColumnLevelAttributesBuilderApi
import io.github.voytech.tabulate.api.builder.dsl.RowLevelAttributesBuilderApi
import io.github.voytech.tabulate.api.builder.dsl.TableLevelAttributesBuilderApi
import io.github.voytech.tabulate.model.attributes.cell.enums.contract.CellFill

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

fun <T> CellLevelAttributesBuilderApi<T>.background(block: CellBackgroundAttribute.Builder.() -> Unit) =
    attribute(CellBackgroundAttribute.Builder().apply(block).build())

fun <T> ColumnLevelAttributesBuilderApi<T>.background(block: CellBackgroundAttribute.Builder.() -> Unit) =
    attribute(CellBackgroundAttribute.Builder().apply(block).build())

fun <T> RowLevelAttributesBuilderApi<T>.background(block: CellBackgroundAttribute.Builder.() -> Unit) =
    attribute(CellBackgroundAttribute.Builder().apply(block).build())

fun <T> TableLevelAttributesBuilderApi<T>.background(block: CellBackgroundAttribute.Builder.() -> Unit) =
    attribute(CellBackgroundAttribute.Builder().apply(block).build())