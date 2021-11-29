package io.github.voytech.tabulate.model

import io.github.voytech.tabulate.model.attributes.alias.CellAttribute
import io.github.voytech.tabulate.template.operations.CellValue

/**
 * Defines behaviours for situations when row span setting causes row to collide with downstream rows at runtime.
 */
internal enum class CollidingRowSpanStrategy(private val priority: Int) {
    /**
     When row span causes row to overlay downstream rows, downstream row values are not rendered, but skipped.
     That behaviour is lossy.
     This strategy has lowest possible priority.
     */
    SHADOW(1),
    /**
     When row span causes row to overlay downstream rows, then all custom rows definitions are shifted down by row span value.
     Also next row index to be requested by iterator is advanced by the value of row span.
     This strategy takes precedence over SHADOW and SKIP strategy.
     */
    PUSHBACK(3),
    /**
     When row span causes row to overlay downstream rows, then none of downstream custom rows definitions are shifted.
     Only next row index to be requested by iterator is advanced by the value of row span.
     This strategy takes precedence over SHADOW strategy.
     */
    SKIP(2),
    /**
     When row span causes row to overlay downstream rows, then an exception is thrown.
     This strategy has HIGHEST priority which means that no matter what different strategy will be set on other cell in the row,
     exception will be thrown eventually.
     */
    THROW(4)
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
    val rowSpanMode: CollidingRowSpanStrategy = CollidingRowSpanStrategy.THROW,
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
