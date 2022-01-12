package io.github.voytech.tabulate.model

import io.github.voytech.tabulate.model.attributes.Attributes
import io.github.voytech.tabulate.model.attributes.alias.CellAttribute
import io.github.voytech.tabulate.model.attributes.orEmpty
import io.github.voytech.tabulate.template.operations.CellValue

/**
 * Defines behaviour for situation when row-span causes row to collide with downstream rows at runtime.
 */
internal enum class CollidingRowSpanStrategy(private val priority: Int) {
    /**
     When row span causes row to collide with downstream rows, downstream rows are rendered normally excluding cells which
     have been shadowed by corresponding (same column) upstream row cells row span values.
     WARNING: That behaviour is lossy.
     This strategy has lowest possible priority, and is set to be default strategy.
     */
    SHADOW(1),
    /**
     When row span causes row to collide with downstream rows, then all custom rows definitions are pushed down by row span value.
     Also next row index to be requested by iterator is advanced by the value of row span.
     This strategy takes precedence over SHADOW and SKIP strategy.
     */
    PUSHBACK(3),
    /**
     When row span causes row to collide with downstream rows, then none of downstream row definitions are pushed down.
     Only next row index to be requested by iterator is advanced by the value of row span, which means that all colliding
     downstream rows will be skipped.
     WARNING: That behaviour is lossy.
     This strategy takes precedence over SHADOW strategy.
     */
    SKIP(2),
    /**
     When row span causes row to collide with downstream rows, then an exception is thrown.
     This strategy has HIGHEST priority which means that no matter what different strategy will be set on other cell in the row,
     exception will be thrown eventually.
     */
    THROW(4)
}

/**
 * Defines single cell exposing specific property of exported ]collection element. Contains cell value which may be
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
     * Expression (late evaluation) to be used when rendering cell.
     */
    @get:JvmSynthetic
    val expression: RowCellExpression<T>?,
    /**
     * Defines how many columns will be occupied for particular cell.
     */
    @get:JvmSynthetic
    val colSpan: Int = 1,
    /**
     * Defines how many rows must will be occupied for particular cell.
     */
    @get:JvmSynthetic
    val rowSpan: Int = 1,
    /**
     * Defines strategy to be used when resolving downstream rows, when row span conflicts are detected.
     */
    @get:JvmSynthetic
    val rowSpanMode: CollidingRowSpanStrategy = CollidingRowSpanStrategy.SHADOW,
    /**
     * Set of [CellAttribute] instances that enable customization of table cell appearance
     */
    @get:JvmSynthetic
    val cellAttributes: Attributes<CellAttribute>?
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

@JvmSynthetic
internal operator fun <T> CellDef<T>?.plus(other: CellDef<T>): CellDef<T> {
    return if (this != null) {
        CellDef(
            value = other.value ?: value,
            expression = other.expression ?: expression,
            colSpan = other.colSpan,
            rowSpan = other.rowSpan,
            rowSpanMode = other.rowSpanMode,
            cellAttributes = cellAttributes.orEmpty() + other.cellAttributes.orEmpty()
        )
    } else other
}
