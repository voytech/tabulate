package io.github.voytech.tabulate.model

import io.github.voytech.tabulate.model.attributes.alias.CellAttribute
import io.github.voytech.tabulate.model.attributes.alias.RowAttribute
import io.github.voytech.tabulate.template.context.CellValue
import java.util.function.Predicate

data class RowDef<T> internal constructor(
    val qualifier: RowQualifier<T>,
    val rowAttributes: Set<RowAttribute>?,
    val cellAttributes: Set<CellAttribute>?,
    val cells: Map<ColumnKey<T>, CellDef<T>>?,
) {
    fun shouldApplyWhen(source: SourceRow<T>): Boolean = qualifier.applyWhen?.test(source) ?: false
    fun shouldInsertRow(source: SourceRow<T>): Boolean = qualifier.createWhen?.test(source) ?: false
    fun isApplicable(source: SourceRow<T>): Boolean = shouldApplyWhen(source) || shouldInsertRow(source)
}

fun interface RowPredicate<T> : Predicate<SourceRow<T>>

data class RowIndexDef(
    val index: Int = 0,
    val offsetLabel: String? = null
) : Comparable<RowIndexDef> {

    operator fun plus(increment: Int): RowIndexDef = RowIndexDef(index + increment)

    operator fun inc(): RowIndexDef = RowIndexDef(index + 1)

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