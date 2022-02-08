package io.github.voytech.tabulate.excel.model.attributes

import io.github.voytech.tabulate.api.builder.CellAttributeBuilder
import io.github.voytech.tabulate.api.builder.TableAttributeBuilder
import io.github.voytech.tabulate.api.builder.dsl.*
import io.github.voytech.tabulate.model.attributes.CellAttribute
import io.github.voytech.tabulate.model.attributes.TableAttribute

/**
 * Excel data format attribute.
 * @author Wojciech Mąka
 * @since 0.1.0
 */
data class CellExcelDataFormatAttribute(
    val dataFormat: String
) : CellAttribute<CellExcelDataFormatAttribute>() {

    @TabulateMarker
    class Builder : CellAttributeBuilder<CellExcelDataFormatAttribute>() {
        var value: String by observable("General", "value" to "dataFormat")
        override fun provide(): CellExcelDataFormatAttribute = CellExcelDataFormatAttribute(value)
    }

    override fun overrideWith(other: CellExcelDataFormatAttribute): CellExcelDataFormatAttribute =
        CellExcelDataFormatAttribute(
            dataFormat = takeIfChanged(other, CellExcelDataFormatAttribute::dataFormat)
        )

}

fun <T> CellLevelAttributesBuilderApi<T>.dataFormat(block: CellExcelDataFormatAttribute.Builder.() -> Unit) =
    attribute(CellExcelDataFormatAttribute.Builder().apply(block))

fun <T> RowLevelAttributesBuilderApi<T>.dataFormat(block: CellExcelDataFormatAttribute.Builder.() -> Unit) =
    attribute(CellExcelDataFormatAttribute.Builder().apply(block))

fun <T> ColumnLevelAttributesBuilderApi<T>.dataFormat(block: CellExcelDataFormatAttribute.Builder.() -> Unit) =
    attribute(CellExcelDataFormatAttribute.Builder().apply(block))

fun <T> ColumnLevelAttributesBuilderApi<T>.format(block: () -> String) =
    attribute(CellExcelDataFormatAttribute.Builder().apply { value = block() })

/**
 * Excel data table attribute. Enables sorting and filtering within specific column and row ranges.
 * @author Wojciech Mąka
 * @since 0.1.0
 */
data class FilterAndSortTableAttribute(
    val columnRange: IntRange,
    val rowRange: IntRange
) : TableAttribute<FilterAndSortTableAttribute>() {

    @TabulateMarker
    class Builder : TableAttributeBuilder<FilterAndSortTableAttribute>() {
        var columnRange: IntRange by observable(0 .. 100)
        var rowRange: IntRange by observable(0..65000)
        override fun provide(): FilterAndSortTableAttribute = FilterAndSortTableAttribute(columnRange, rowRange)
    }

    override fun overrideWith(other: FilterAndSortTableAttribute): FilterAndSortTableAttribute = FilterAndSortTableAttribute(
        columnRange = takeIfChanged(other, FilterAndSortTableAttribute::columnRange),
        rowRange = takeIfChanged(other, FilterAndSortTableAttribute::rowRange),
    )

}

fun <T> TableLevelAttributesBuilderApi<T>.filterAndSort(block: FilterAndSortTableAttribute.Builder.() -> Unit) =
    attribute(FilterAndSortTableAttribute.Builder().apply(block))
