package io.github.voytech.tabulate.model

import io.github.voytech.tabulate.model.attributes.alias.CellAttribute
import io.github.voytech.tabulate.template.operations.CellValue

/**
 * Defines behaviours for cases when row span setting causes row to overlay downstream rows.
 */
internal enum class CollidingRowSpanStrategy {
    /**
     When row span causes row to overlay downstream rows, downstream row values are not rendered, but skipped.
     That behaviours is lossy.
     */
    SHADOW,
    /**
    When row span causes row to overlay downstream rows, then all custom rows definitions are shifted down by row span value.
    Also next row index to be requested by iterator is advanced by the value of row span.
     */
    SHIFT,
    /**
    When row span causes row to overlay downstream rows, then none of downstream custom rows definitions are shifted.
    Only next row index to be requested by iterator is advanced by the value of row span.
     */
    SKIP,
    /**
    When row span causes row to overlay downstream rows, then an exception is thrown.
     */
    THROW
}

internal data class CellDef<T> internal constructor(
    @get:JvmSynthetic
    val value: Any?,
    @get:JvmSynthetic
    val expression: RowCellExpression<T>?,
    @get:JvmSynthetic
    val colSpan: Int = 1,
    @get:JvmSynthetic
    val rowSpan: Int = 1,
    @get:JvmSynthetic
    val rowSpanMode: CollidingRowSpanStrategy = CollidingRowSpanStrategy.SHADOW,
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
                it.colSpan,
                it.rowSpan
            )
        }
    } ?: column.resolveRawValue(maybeRow?.record)?.let { CellValue(it) }
}
