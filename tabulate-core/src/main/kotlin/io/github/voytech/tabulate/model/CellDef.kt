package io.github.voytech.tabulate.model

import io.github.voytech.tabulate.model.attributes.alias.CellAttribute
import io.github.voytech.tabulate.template.operations.CellValue

/**
 * Defines behaviour for situation when row-span causes row to collide with downstream rows at runtime.
 */
internal enum class CollidingRowSpanStrategy(private val priority: Int) {
    /**
     When row span causes row to overlay downstream rows, downstream row values are not rendered, but skipped.
     WARNING: That behaviour is lossy.
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

/**
 * Defines single cell exposing specific property of a data-set record being rendered. Contains cell value which may be
 * evaluated in several ways:
 * - by evaluating property getter function on processed object (row)
 * - by evaluating expression [RowCellExpression]
 * - by injecting predefined value [value]
 * [CellDef] can also control [colSpan] and [rowSpan] attributes as well as customize [rowSpanMode]
 */
internal data class CellDef<T> internal constructor(
    /**
     * Predefined value to be used when rendering cell.
     */
    @get:JvmSynthetic
    val value: Any?,
    /**
     * Expression (late value evaluation) to be used when rendering cell.
     */
    @get:JvmSynthetic
    val expression: RowCellExpression<T>?,
    /**
     * Defines how many columns will be occupied for specific cell.
     */
    @get:JvmSynthetic
    val colSpan: Int = 1,
    /**
     * Defines how many rows must will be occupied for specific cell.
     */
    @get:JvmSynthetic
    val rowSpan: Int = 1,
    /**
     * Defines strategy to be used when resolving downstream rows, when row span conflicts are detected.
     */
    @get:JvmSynthetic
    val rowSpanMode: CollidingRowSpanStrategy = CollidingRowSpanStrategy.THROW,
    /**
     * Aggregates set of [CellAttribute] that enable customization of table cell appearance
     */
    @get:JvmSynthetic
    val cellAttributes: Set<CellAttribute>?
) {

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
