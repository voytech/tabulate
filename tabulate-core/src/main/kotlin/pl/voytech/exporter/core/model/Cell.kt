package pl.voytech.exporter.core.model

import pl.voytech.exporter.core.model.attributes.alias.CellAttribute
import pl.voytech.exporter.core.template.context.CellValue

data class Cell<T> internal constructor(
    val value: Any?,
    val eval: RowCellEval<T>?,
    val type: CellType?,
    val colSpan: Int = 1,
    val rowSpan: Int = 1,
    val cellAttributes: Set<CellAttribute>?
) {
    fun colSpanOffset() = colSpan - 1

    fun rowSpanOffset() = rowSpan - 1

    internal fun resolveRawValue(context: SourceRow<T>? = null) : Any? = context?.let { eval?.invoke(it) } ?: value
}

internal fun <T> Cell<T>?.resolveCellValue(column: Column<T>, maybeRow: SourceRow<T>? = null): CellValue? {
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
