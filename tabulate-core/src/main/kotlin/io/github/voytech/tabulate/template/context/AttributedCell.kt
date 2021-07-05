package io.github.voytech.tabulate.template.context

import io.github.voytech.tabulate.model.attributes.alias.CellAttribute

data class AttributedCell(
    val value: CellValue,
    val attributes: Set<CellAttribute>?,
    val rowIndex: Int,
    val columnIndex: Int,
): ContextData<Unit>(), RowCellCoordinate {
    override fun getRow(): Int = rowIndex
    override fun getColumn(): Int = columnIndex
}


internal fun <T> TableExportingState<T>.createAttributedCell(
    relativeRowIndex: Int,
    relativeColumnIndex: Int,
    value: CellValue,
    attributes: Set<CellAttribute>,
): AttributedCell {
    return AttributedCell(
        value = value,
        attributes = attributes,
        rowIndex = (firstRow ?: 0) + relativeRowIndex,
        columnIndex = (firstColumn ?: 0) + relativeColumnIndex,
    ).apply { additionalAttributes = getCustomAttributes() }
}