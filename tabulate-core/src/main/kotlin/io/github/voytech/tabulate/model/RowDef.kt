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
    internal fun shouldApplyWhen(source: SourceRow<T>): Boolean = qualifier.applyWhen?.test(source) ?: false
    @JvmSynthetic
    internal fun shouldInsertRow(source: SourceRow<T>): Boolean = qualifier.createWhen?.test(source) ?: false
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
    val offsetLabel: String? = null,
) : Comparable<RowIndexDef> {

    operator fun plus(increment: Int): RowIndexDef = RowIndexDef(index + increment, offsetLabel)

    operator fun inc(): RowIndexDef = RowIndexDef(index + 1, offsetLabel)

    override fun compareTo(other: RowIndexDef): Int = index.compareTo(other.index)
}

data class RowQualifier<T>(
    val applyWhen: RowPredicate<T>? = null,
    val createWhen: RowPredicate<T>? = null,
    val createAt: RowIndexDef? = null,
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