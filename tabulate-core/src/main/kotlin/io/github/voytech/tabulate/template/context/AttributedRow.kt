package io.github.voytech.tabulate.template.context

import io.github.voytech.tabulate.model.ColumnKey
import io.github.voytech.tabulate.model.attributes.alias.RowAttribute

data class AttributedRow<T>(
    val rowAttributes: Set<RowAttribute>?,
    val rowCellValues: Map<ColumnKey<T>, AttributedCell>,
    val rowIndex: Int
): ContextData<T>(), RowCoordinate {
    override fun getRow(): Int = rowIndex
}

internal fun <T> TableExportingState<T>.createAttributedRow(
    relativeRowIndex: Int,
    rowAttributes: Set<RowAttribute>,
    cells: Map<ColumnKey<T>, AttributedCell>,
): AttributedRow<T> {
    return AttributedRow(
        rowIndex = (firstRow ?: 0) + relativeRowIndex,
        rowAttributes = rowAttributes,
        rowCellValues = cells
    ).apply { additionalAttributes = getCustomAttributes() }
}