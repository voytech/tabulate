package io.github.voytech.tabulate.core.template.context

import io.github.voytech.tabulate.core.model.attributes.alias.CellAttribute

data class AttributedCell(
    val value: CellValue,
    val attributes: Set<CellAttribute>?,
    val rowIndex: Int,
    val columnIndex: Int,
): ContextData<Unit>(), RowCellCoordinate {
    override fun getRow(): Int = rowIndex
    override fun getColumn(): Int = columnIndex
}
