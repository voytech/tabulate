package io.github.voytech.tabulate.excel.model.attributes

import io.github.voytech.tabulate.api.builder.CellAttributeBuilder
import io.github.voytech.tabulate.api.builder.TableAttributeBuilder
import io.github.voytech.tabulate.api.builder.dsl.*
import io.github.voytech.tabulate.model.attributes.CellAttribute
import io.github.voytech.tabulate.model.attributes.TableAttribute

data class CellExcelDataFormatAttribute(
    val dataFormat: String
) : CellAttribute<CellExcelDataFormatAttribute>() {

    @TabulateMarker
    class Builder : CellAttributeBuilder<CellExcelDataFormatAttribute>() {
        var value: String = "General"
        override fun provide(): CellExcelDataFormatAttribute = CellExcelDataFormatAttribute(value)
    }

}

fun dataFormat(block: CellExcelDataFormatAttribute.Builder.() -> Unit): CellExcelDataFormatAttribute = CellExcelDataFormatAttribute.Builder().apply(block).build()

fun <T> TableLevelAttributesBuilderApi<T>.dataTestAttributeRenderOperationsProviderFormat(block: CellExcelDataFormatAttribute.Builder.() -> Unit) =
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

    @TabulateMarker
    class Builder : TableAttributeBuilder {
        var columnRange: IntRange = (0 .. 100)
        var rowRange: IntRange = (0..65000)
        override fun build(): FilterAndSortTableAttribute = FilterAndSortTableAttribute(columnRange, rowRange)
    }

}

fun <T> TableLevelAttributesBuilderApi<T>.filterAndSort(block: FilterAndSortTableAttribute.Builder.() -> Unit) =
    attribute(FilterAndSortTableAttribute.Builder().apply(block).build())
