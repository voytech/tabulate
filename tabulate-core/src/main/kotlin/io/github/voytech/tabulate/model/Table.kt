package io.github.voytech.tabulate.model

import io.github.voytech.tabulate.model.attributes.alias.CellAttribute
import io.github.voytech.tabulate.model.attributes.alias.ColumnAttribute
import io.github.voytech.tabulate.model.attributes.alias.RowAttribute
import io.github.voytech.tabulate.model.attributes.alias.TableAttribute
import io.github.voytech.tabulate.template.context.RowIndex
import java.util.function.Consumer

/**
 * A top-level definition of tabular layout.
 *   @author Wojciech Mąka
 */
class Table<T> internal constructor(
    @get:JvmSynthetic
    internal val name: String = "untitled",
    @get:JvmSynthetic
    internal val firstRow: Int? = 0,
    @get:JvmSynthetic
    internal val firstColumn: Int? = 0,
    @get:JvmSynthetic
    internal val columns: List<ColumnDef<T>> = emptyList(),
    @get:JvmSynthetic
    internal val rows: List<RowDef<T>>?,
    @get:JvmSynthetic
    internal val tableAttributes: Set<TableAttribute>?,
    @get:JvmSynthetic
    internal val cellAttributes: Set<CellAttribute>?,
    @get:JvmSynthetic
    internal val columnAttributes: Set<ColumnAttribute>?,
    @get:JvmSynthetic
    internal val rowAttributes: Set<RowAttribute>?
) {
    private var indexedCustomRows: Map<RowIndexDef, List<RowDef<T>>>? = null

    init {
        indexedCustomRows = rows?.filter { it.qualifier.createAt != null }
            ?.sortedBy { it.qualifier.createAt!! }
            ?.groupBy { it.qualifier.createAt!! }
    }

    @JvmSynthetic
    internal inline fun forEachColumn(consumer: (Int, ColumnDef<T>) -> Unit) = columns.forEachIndexed(consumer)

    @JvmSynthetic
    internal fun forEachRow(consumer: Consumer<in RowDef<T>>) = rows?.forEach(consumer)

    @JvmSynthetic
    internal fun getRowsAt(index: RowIndex): List<RowDef<T>>? {
        return if (index.labels.isEmpty()) {
            indexedCustomRows?.get(RowIndexDef(index.rowIndex))
        } else {
            index.labels.mapNotNull {
                indexedCustomRows?.get(RowIndexDef(index = it.value.index, offsetLabel = it.key))
            }.flatten()
        }
    }

    private fun hasRowsAt(index: RowIndex): Boolean = !getRowsAt(index).isNullOrEmpty()

    @JvmSynthetic
    internal fun getNextCustomRowIndex(index: RowIndex): RowIndexDef? {
        return indexedCustomRows?.entries
            ?.firstOrNull { it.key.index > index.rowIndex }
            ?.key
    }

    @JvmSynthetic
    internal fun getRows(sourceRow: SourceRow<T>): Set<RowDef<T>> {
        val customRows = getRowsAt(sourceRow.rowIndex)?.toSet()
        val matchingRows = rows?.filter { it.isApplicable(sourceRow) }?.toSet()
        return customRows?.let { matchingRows?.plus(it) ?: it } ?: matchingRows ?: emptySet()
    }

    @JvmSynthetic
    internal fun hasCustomRows(sourceRow: SourceRow<T>): Boolean {
        return hasRowsAt(sourceRow.rowIndex) || rows?.any { it.shouldInsertRow(sourceRow) } ?: false
    }
}
