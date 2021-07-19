package io.github.voytech.tabulate.template.context

import io.github.voytech.tabulate.model.Table
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

internal fun <T> Table<T>.createAttributedCell(
    rowIndex: Int,
    columnIndex: Int,
    value: CellValue,
    attributes: Set<CellAttribute>,
    customAttributes: MutableMap<String, Any>
): AttributedCell {
    return AttributedCell(
        value = value,
        attributes = attributes,
        rowIndex = (firstRow ?: 0) + rowIndex,
        columnIndex = (firstColumn ?: 0) + columnIndex,
    ).apply { additionalAttributes = customAttributes }
}