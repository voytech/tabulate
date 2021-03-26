package pl.voytech.exporter.core.model.attributes.table

import pl.voytech.exporter.core.model.attributes.TableAttribute

data class FilterAndSortTableAttribute(
    val columnRange: IntRange,
    val rowRange: IntRange
) : TableAttribute<FilterAndSortTableAttribute>() {
    override fun mergeWith(other: FilterAndSortTableAttribute): FilterAndSortTableAttribute = FilterAndSortTableAttribute(
        columnRange = other.columnRange,
        rowRange = other.rowRange
    )
}