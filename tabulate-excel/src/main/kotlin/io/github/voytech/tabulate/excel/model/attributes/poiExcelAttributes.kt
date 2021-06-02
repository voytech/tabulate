package io.github.voytech.tabulate.excel.model.attributes

import io.github.voytech.tabulate.api.builder.CellAttributeBuilder
import io.github.voytech.tabulate.api.builder.TableAttributeBuilder
import io.github.voytech.tabulate.api.builder.dsl.CellLevelAttributesBuilderApi
import io.github.voytech.tabulate.api.builder.dsl.ColumnLevelAttributesBuilderApi
import io.github.voytech.tabulate.api.builder.dsl.RowLevelAttributesBuilderApi
import io.github.voytech.tabulate.api.builder.dsl.TableLevelAttributesBuilderApi
import io.github.voytech.tabulate.model.attributes.CellAttribute
import io.github.voytech.tabulate.model.attributes.TableAttribute

data class CellExcelDataFormatAttribute(
    val dataFormat: String
) : CellAttribute<CellExcelDataFormatAttribute>() {
    class Builder : CellAttributeBuilder<CellExcelDataFormatAttribute> {
        var value: String = "General"
        override fun build(): CellExcelDataFormatAttribute = CellExcelDataFormatAttribute(value)
    }
    override fun mergeWith(other: CellExcelDataFormatAttribute): CellExcelDataFormatAttribute  = other
}

fun dataFormat(block: CellExcelDataFormatAttribute.Builder.() -> Unit): CellExcelDataFormatAttribute = CellExcelDataFormatAttribute.Builder().apply(block).build()

fun <T> TableLevelAttributesBuilderApi<T>.dataFormat(block: CellExcelDataFormatAttribute.Builder.() -> Unit) =
    attribute(CellExcelDataFormatAttribute.Builder().apply(block).build())

fun <T> CellLevelAttributesBuilderApi<T>.dataFormat(block: CellExcelDataFormatAttribute.Builder.() -> Unit) =
    attribute(CellExcelDataFormatAttribute.Builder().apply(block).build())

fun <T> RowLevelAttributesBuilderApi<T>.dataFormat(block: CellExcelDataFormatAttribute.Builder.() -> Unit) =
    attribute(CellExcelDataFormatAttribute.Builder().apply(block).build())

fun <T> ColumnLevelAttributesBuilderApi<T>.dataFormat(block: CellExcelDataFormatAttribute.Builder.() -> Unit) =
    attribute(CellExcelDataFormatAttribute.Builder().apply(block).build())


data class FilterAndSortTableAttribute(
    val columnRange: IntRange,
    val rowRange: IntRange
) : TableAttribute<FilterAndSortTableAttribute>() {

    class Builder : TableAttributeBuilder {
        var columnRange: IntRange = (0 .. 100)
        var rowRange: IntRange = (0..65000)
        override fun build(): FilterAndSortTableAttribute = FilterAndSortTableAttribute(columnRange, rowRange)
    }

    override fun mergeWith(other: FilterAndSortTableAttribute): FilterAndSortTableAttribute = FilterAndSortTableAttribute(
        columnRange = other.columnRange,
        rowRange = other.rowRange
    )
}

fun <T> TableLevelAttributesBuilderApi<T>.filterAndSort(block: FilterAndSortTableAttribute.Builder.() -> Unit) =
    attribute(FilterAndSortTableAttribute.Builder().apply(block).build())