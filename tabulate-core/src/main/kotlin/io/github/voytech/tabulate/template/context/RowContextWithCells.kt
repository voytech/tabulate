package io.github.voytech.tabulate.template.context

import io.github.voytech.tabulate.model.ColumnKey

open class RowContext<T>(open val rowIndex: Int) : ContextData(), RowCoordinate {
    override fun getRow(): Int = rowIndex
}

data class RowContextWithCells<T>(
    val rowCellValues: Map<ColumnKey<T>, RowCellContext>,
    override val rowIndex: Int,
) : RowContext<T>(rowIndex), RowCoordinate {
    override fun getRow(): Int = rowIndex
}

fun <T> AttributedRowWithCells<T>.crop(): RowContextWithCells<T> =
    RowContextWithCells(rowCellValues.crop(), rowIndex).also { it.additionalAttributes = additionalAttributes }

fun <T> AttributedRow<T>.crop(): RowContext<T> =
    RowContext<T>(rowIndex).also { it.additionalAttributes = additionalAttributes }

private fun <T> Map<ColumnKey<T>, AttributedCell>.crop(): Map<ColumnKey<T>,RowCellContext> {
    return entries.associate {
        it.key to it.value.crop()
    }
}