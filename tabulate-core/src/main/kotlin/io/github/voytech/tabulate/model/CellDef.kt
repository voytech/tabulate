package io.github.voytech.tabulate.model

import io.github.voytech.tabulate.model.attributes.alias.CellAttribute
import io.github.voytech.tabulate.template.context.CellValue

internal data class CellDef<T> internal constructor(
    @get:JvmSynthetic
    val value: Any?,
    @get:JvmSynthetic
    val expression: RowCellExpression<T>?,
    @get:JvmSynthetic
    val type: CellType?,
    @get:JvmSynthetic
    val colSpan: Int = 1,
    @get:JvmSynthetic
    val rowSpan: Int = 1,
    @get:JvmSynthetic
    val cellAttributes: Set<CellAttribute>?
) {
    @JvmSynthetic
    fun colSpanOffset() = colSpan - 1

    @JvmSynthetic
    fun rowSpanOffset() = rowSpan - 1

    @JvmSynthetic
    internal fun resolveRawValue(context: SourceRow<T>? = null) : Any? = context?.let { expression?.evaluate(it) } ?: value
}

@JvmSynthetic
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
