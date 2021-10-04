package io.github.voytech.tabulate.model

import io.github.voytech.tabulate.api.builder.fluent.TableBuilder
import io.github.voytech.tabulate.model.attributes.alias.CellAttribute
import io.github.voytech.tabulate.model.attributes.alias.ColumnAttribute
import io.github.voytech.tabulate.model.attributes.alias.RowAttribute
import io.github.voytech.tabulate.model.attributes.alias.TableAttribute
import io.github.voytech.tabulate.template.context.RowIndex
import java.util.function.Consumer
import io.github.voytech.tabulate.api.builder.TableBuilder as TableBuilderBase

/**
 * A top-level definition of tabular layout.
 *   @author Wojciech Mąka
 */
data class Table<T> internal constructor(
    val name: String = "untitled",
    val firstRow: Int? = 0,
    val firstColumn: Int? = 0,
    val columns: List<ColumnDef<T>> = emptyList(),
    val rows: List<RowDef<T>>?,
    val tableAttributes: Set<TableAttribute>?,
    val cellAttributes: Set<CellAttribute>?,
    val columnAttributes: Set<ColumnAttribute>?,
    val rowAttributes: Set<RowAttribute>?
) {
    private var indexedCustomRows: Map<RowIndexDef, List<RowDef<T>>>? = null

    init {
        indexedCustomRows = rows?.filter { it.qualifier.createAt != null }
            ?.sortedBy { it.qualifier.createAt!! }
            ?.groupBy { it.qualifier.createAt!! }
    }

    internal inline fun forEachColumn(consumer: (Int, ColumnDef<T>) -> Unit) = columns.forEachIndexed(consumer)

    internal fun forEachRow(consumer: Consumer<in RowDef<T>>) = rows?.forEach(consumer)

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

    internal fun getNextCustomRowIndex(index: RowIndex): RowIndexDef? {
        return indexedCustomRows?.entries
            ?.firstOrNull { it.key.index > index.rowIndex }
            ?.key
    }

    internal fun getRows(sourceRow: SourceRow<T>): Set<RowDef<T>> {
        val customRows = getRowsAt(sourceRow.rowIndex)?.toSet()
        val matchingRows = rows?.filter { it.isApplicable(sourceRow) }?.toSet()
        return customRows?.let { matchingRows?.plus(it) ?: it } ?: matchingRows ?: emptySet()
    }

    internal fun hasCustomRows(sourceRow: SourceRow<T>): Boolean {
        return hasRowsAt(sourceRow.rowIndex) || rows?.any { it.shouldInsertRow(sourceRow) } ?: false
    }

    companion object {
        @JvmStatic
        fun <T> builder() = TableBuilder<T>(TableBuilderBase())
    }
}
