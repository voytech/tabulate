package io.github.voytech.tabulate.model

import io.github.voytech.tabulate.model.attributes.alias.CellAttribute
import io.github.voytech.tabulate.model.attributes.alias.RowAttribute
import io.github.voytech.tabulate.template.context.CellValue

internal data class RowDef<T> internal constructor(
    @get:JvmSynthetic
    internal val qualifier: RowQualifier<T>,
    @get:JvmSynthetic
    internal val rowAttributes: Set<RowAttribute>?,
    @get:JvmSynthetic
    internal val cellAttributes: Set<CellAttribute>?,
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
)

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