package io.github.voytech.tabulate.model

import io.github.voytech.tabulate.model.attributes.alias.CellAttribute
import io.github.voytech.tabulate.template.context.CellValue

data class CellDef<T> internal constructor(
    val value: Any?,
    val expression: RowCellExpression<T>?,
    val type: CellType?,
    val colSpan: Int = 1,
    val rowSpan: Int = 1,
    val cellAttributes: Set<CellAttribute>?
) {
    fun colSpanOffset() = colSpan - 1

    fun rowSpanOffset() = rowSpan - 1

    internal fun resolveRawValue(context: SourceRow<T>? = null) : Any? = context?.let { expression?.evaluate(it) } ?: value
}

internal fun <T> CellDef<T>?.resolveCellValue(column: ColumnDef<T>, maybeRow: SourceRow<T>? = null): CellValue? {
    return this?.let {
        it.resolveRawValue(maybeRow)?.let { rawValue ->
            CellValue(
                rawValue,
                (it.type ?: column.columnType).orProbe(rawValue),
                it.colSpan,
                it.rowSpan
            )
        }
    } ?: column.resolveRawValue(maybeRow?.record)?.let { CellValue(it, column.columnType.orProbe(it)) }
}