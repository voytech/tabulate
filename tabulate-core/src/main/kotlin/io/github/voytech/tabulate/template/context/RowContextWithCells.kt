package io.github.voytech.tabulate.template.context

import io.github.voytech.tabulate.model.ColumnKey
import io.github.voytech.tabulate.model.attributes.RowAttribute

open class RowContext<T>(private val attributedContext: AttributedRow<T>) :
        Context by attributedContext,
        RowCoordinate by attributedContext,
        ModelAttributeAccessor<RowAttribute<*>>(attributedContext)

class RowContextWithCells<T>(private val attributedContext: AttributedRowWithCells<T>) : RowContext<T>(attributedContext) {
    fun getCells(): Map<ColumnKey<T>, RowCellContext> = attributedContext.rowCellValues.crop()
}

fun <T> AttributedRowWithCells<T>.crop(): RowContextWithCells<T> = RowContextWithCells(this)

fun <T> AttributedRow<T>.crop(): RowContext<T> = RowContext(this)

private fun <T> Map<ColumnKey<T>, AttributedCell>.crop(): Map<ColumnKey<T>, RowCellContext> {
    return entries.associate {
        it.key to it.value.crop()
    }
}