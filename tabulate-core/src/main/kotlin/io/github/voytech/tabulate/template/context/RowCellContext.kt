package io.github.voytech.tabulate.template.context

data class RowCellContext(
    val value: CellValue,
    val rowIndex: Int,
    val columnIndex: Int,
) : ContextData(), RowCellCoordinate {
    override fun getRow(): Int = rowIndex
    override fun getColumn(): Int = columnIndex
}

fun AttributedCell.crop(): RowCellContext =
    RowCellContext(value, rowIndex, columnIndex).also { it.additionalAttributes = additionalAttributes }
