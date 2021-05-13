package pl.voytech.exporter.core.template.context

import pl.voytech.exporter.core.model.ColumnKey

data class RowContext<T>(
    val rowCellValues: Map<ColumnKey<T>, AttributedCell>,
    val rowIndex: Int,
) : ContextData<Unit>(), RowCoordinate {
    override fun getRow(): Int = rowIndex
}

fun <T> AttributedRow<T>.narrow(): RowContext<T> =
    RowContext<T>(rowCellValues, rowIndex).also { it.additionalAttributes = additionalAttributes }
