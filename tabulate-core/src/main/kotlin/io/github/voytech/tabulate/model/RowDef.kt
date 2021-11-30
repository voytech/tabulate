package io.github.voytech.tabulate.model

import io.github.voytech.tabulate.model.attributes.alias.CellAttribute
import io.github.voytech.tabulate.model.attributes.alias.RowAttribute
import io.github.voytech.tabulate.template.operations.CellValue

/**
 * Defines single row in table which represents single record from exported data-set.
 * May also contain custom cell values (e.g. to display table header or footer).
 * @author Wojciech Mąka
 */
internal data class RowDef<T> internal constructor(
    /**
     * Given [SourceRow] qualifies specific row definition for row context resolving.
     * When row definition is matched for specific [SourceRow] then:
     * - all its row attributes as well as cell attributes are used to build row context for renderer.
     * - all custom cells are merged with other custom cells from other row definitions matching specific [SourceRow].
     */
    @get:JvmSynthetic
    internal val qualifier: RowQualifier<T>,
    /**
     * Row level attributes to be used in row context while rendering row.
     */
    @get:JvmSynthetic
    internal val rowAttributes: Set<RowAttribute>?,
    /**
     * Cell level attributes to be used in row cell context while rendering cell.
     */
    @get:JvmSynthetic
    internal val cellAttributes: Set<CellAttribute>?,
    /**
     * All custom cell definitions. Each cell definition may contain custom value and/or cell attributes to be applied on
     * value resolved from data-set record.
     */
    @get:JvmSynthetic
    internal val cells: Map<ColumnKey<T>, CellDef<T>>?,
) {
    @JvmSynthetic
    internal fun shouldApplyWhen(source: SourceRow<T>): Boolean = qualifier.matching?.test(source) ?: false

    @JvmSynthetic
    internal fun shouldInsertRow(source: SourceRow<T>): Boolean = qualifier.index?.test(source) ?: false

    @JvmSynthetic
    internal fun isApplicable(source: SourceRow<T>): Boolean = shouldApplyWhen(source) || shouldInsertRow(source)
}

@JvmSynthetic
internal fun <T> Collection<RowDef<T>>.flattenRowAttributes(): Set<RowAttribute> =
    mapNotNull { it.rowAttributes }.fold(setOf()) { acc, r -> acc + r }

@JvmSynthetic
internal fun <T> Collection<RowDef<T>>.flattenCellAttributes(): Set<CellAttribute> =
    mapNotNull { it.cellAttributes }.fold(setOf()) { acc, r -> acc + r }

@JvmSynthetic
internal fun <T> Collection<RowDef<T>>.mergeCells(): Map<ColumnKey<T>, CellDef<T>> =
    mapNotNull { it.cells }.fold(mapOf()) { acc, m -> acc + m }


data class RowIndexDef(
    val index: Int = 0,
    val step: Enum<*>? = null,
) : Comparable<RowIndexDef> {

    operator fun plus(increment: Int): RowIndexDef = RowIndexDef(index + increment, step)

    operator fun minus(increment: Int): RowIndexDef =
        RowIndexDef((index - increment).coerceAtLeast(0), step)

    operator fun inc(): RowIndexDef = RowIndexDef(index + 1, step)

    override fun compareTo(other: RowIndexDef): Int = index.compareTo(other.index)

    companion object {
        fun maxValue(step: Enum<*>?) = RowIndexDef(Int.MAX_VALUE, step)
        fun minValue(step: Enum<*>?) = RowIndexDef(0, step)
    }
}

internal fun ClosedRange<RowIndexDef>.progression(): IntProgression =
    IntProgression.fromClosedRange(start.index, endInclusive.index, 1)

internal fun ClosedRange<RowIndexDef>.materialize(): Set<RowIndexDef> =
    progression().map { RowIndexDef(it, start.step) }.toSet()


data class RowQualifier<T>(
    val matching: RowPredicate<T>? = null,
    val index: RowIndexPredicateLiteral<T>? = null,
) {
    companion object {
        fun <T> index(predicateLiteral: RowIndexPredicateLiteral<T>): RowQualifier<T> =
            RowQualifier(index = predicateLiteral)
    }
}

internal fun <T> Map<ColumnKey<T>, CellDef<T>>?.resolveCellValue(
    column: ColumnDef<T>,
    maybeRow: SourceRow<T>? = null,
): CellValue? {
    return this?.get(column.id).resolveCellValue(column, maybeRow)
}

@JvmName("resolveCell2")
internal fun <T> Map<ColumnDef<T>, CellDef<T>>?.resolveCellValue(
    column: ColumnDef<T>,
    maybeRow: SourceRow<T>? = null,
): CellValue? {
    return this?.get(column).resolveCellValue(column, maybeRow)
}