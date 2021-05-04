package pl.voytech.exporter.impl.template.model.attributes

import pl.voytech.exporter.core.api.builder.CellAttributeBuilder
import pl.voytech.exporter.core.api.builder.TableAttributeBuilder
import pl.voytech.exporter.core.model.attributes.CellAttribute
import pl.voytech.exporter.core.model.attributes.TableAttribute

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

fun filterAndSort(block: FilterAndSortTableAttribute.Builder.() -> Unit): TableAttribute<FilterAndSortTableAttribute> = FilterAndSortTableAttribute.Builder().apply(block).build()
