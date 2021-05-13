package pl.voytech.exporter.core.template.context

import pl.voytech.exporter.core.model.attributes.alias.CellAttribute

data class AttributedCell(
    val value: CellValue,
    val attributes: Set<CellAttribute>?,
    val rowIndex: Int,
    val columnIndex: Int,
): ContextData<Unit>(), RowCellCoordinate {
    override fun getRow(): Int = rowIndex
    override fun getColumn(): Int = columnIndex
}
