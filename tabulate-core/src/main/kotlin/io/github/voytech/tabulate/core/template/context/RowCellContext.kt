package io.github.voytech.tabulate.core.template.context

data class RowCellContext(
    val value: CellValue,
    val rowIndex: Int,
    val columnIndex: Int,
) : ContextData<Unit>(), RowCellCoordinate {
    override fun getRow(): Int = rowIndex
    override fun getColumn(): Int = columnIndex
}

fun AttributedCell.narrow(): RowCellContext =
    RowCellContext(value, rowIndex, columnIndex).also { it.additionalAttributes = additionalAttributes }
