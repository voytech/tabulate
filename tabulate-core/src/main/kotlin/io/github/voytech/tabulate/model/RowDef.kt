package io.github.voytech.tabulate.model

import io.github.voytech.tabulate.model.attributes.Attributes
import io.github.voytech.tabulate.model.attributes.CellAttribute
import io.github.voytech.tabulate.model.attributes.RowAttribute
import io.github.voytech.tabulate.template.operations.CellValue

/**
 * Defines single row in table which represents single record from exported data-set.
 * May also contain custom cell values (e.g. to display table header or footer).
 * @author Wojciech Mąka
 * @since 0.1.0
 */
internal data class RowDef<T> internal constructor(
    /**
     * Takes [SourceRow] instance and tests it against row definition qualification rules (within [RowQualifier]).
     *
     * When row definition is matched for specific [SourceRow] then:
     * - all its row attributes as well as cell attributes are used to build row context for row rendering/exporting.
     * - all custom cells are merged with other custom cells from other row definitions matching this particular [SourceRow].
     */
    @get:JvmSynthetic
    internal val qualifier: RowQualifier<T>,
    /**
     * Row level attributes to be used in row context while rendering row.
     */
    @get:JvmSynthetic
    internal val rowAttributes: Attributes<RowAttribute<*>>?,
    /**
     * Cell level attributes to be used in row cell context while rendering cell.
     */
    @get:JvmSynthetic
    internal val cellAttributes: Attributes<CellAttribute<*>>?,
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
internal fun <T> Collection<RowDef<T>>.flattenRowAttributes(): Attributes<RowAttribute<*>> =
    mapNotNull { it.rowAttributes }.fold(Attributes(attributeCategory = RowAttribute::class.java)) { acc, r -> acc + r }

@JvmSynthetic
internal fun <T> Collection<RowDef<T>>.flattenCellAttributes(): Attributes<CellAttribute<*>> =
    mapNotNull { it.cellAttributes }.fold(Attributes(attributeCategory = CellAttribute::class.java)) { acc, r -> acc + r }

@JvmSynthetic
internal fun <T> Collection<RowDef<T>>.mergeCells(): Map<ColumnKey<T>, CellDef<T>> =
    mapNotNull { it.cells }.fold(mapOf()) { acc, m -> acc + m }

private operator fun <T> Map<ColumnKey<T>, CellDef<T>>.plus(map: Map<ColumnKey<T>, CellDef<T>>): Map<ColumnKey<T>, CellDef<T>> =
    LinkedHashMap(this).apply {
        map.keys.forEach { columnKey ->
            this[columnKey] = this[columnKey] + map[columnKey]!!
        }
    }

/**
 * Complex index definition. It can act as absolute index when no step enum is provided (indexing starts from the very
 * first row being exported), or index value relative to specific exporting phase (step). This is required in order
 * to define row position when we cannot tell absolute index at definition time. Currently it only happens when we
 * want to insert footer row and do not know about size of exported collection, because it can vary at run-time.
 * @author Wojciech Mąka
 * @since 0.1.0
 */
data class RowIndexDef(
    val index: Int = 0,
    val step: Enum<*>? = null,
) : Comparable<RowIndexDef> {

    operator fun plus(increment: Int): RowIndexDef = RowIndexDef(index + increment, step)

    operator fun minus(increment: Int): RowIndexDef =
        RowIndexDef((index - increment).coerceAtLeast(0), step)

    operator fun inc(): RowIndexDef = RowIndexDef(index + 1, step)

    override fun compareTo(other: RowIndexDef): Int {
        return when {
            step == other.step -> index.compareTo(other.index)
            step == null && other.step != null -> -1
            step != null && other.step == null -> 1
            step != null && other.step != null -> step.ordinal.compareTo(other.step.ordinal)
            else -> 0
        }
    }

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

fun interface RowCellExpression<T> {
    fun evaluate(context: SourceRow<T>): Any?
}

internal fun <T> Map<ColumnKey<T>, CellDef<T>>?.resolveCellValue(
    column: ColumnDef<T>,
    maybeRow: SourceRow<T>? = null,
): CellValue? {
    return this?.get(column.id).resolveCellValue(column, maybeRow)
}
