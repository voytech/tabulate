package io.github.voytech.tabulate.model

import io.github.voytech.tabulate.api.builder.fluent.TableBuilder
import io.github.voytech.tabulate.model.attributes.alias.CellAttribute
import io.github.voytech.tabulate.model.attributes.alias.TableAttribute
import java.util.function.Consumer
import io.github.voytech.tabulate.api.builder.TableBuilder as TableBuilderBase

/**
 * A top-level model class. Defines how records from repositories will be handled by file rendering strategies.
 *
 * special property class - '...extensions' holds a list of 'custom attributes' for enriching
 * capabilities of exporters for different output formats.
 * It has been called 'extensions' rather than 'customAttributes' because it should be possible to:
 * - always export data without any extensions applied using each implemented exporter,
 * - custom attributes reserved for particular exporter does not restricts model for being exported only by this
 *   particular exporter,
 * - effectively any model with any custom attribute should be compatible with any exporter,
 * - 'extension' seems to better describe the model entity than 'custom attribute' as custom attribute seems to have the same
 *   priority as canonical attributes (fields of Table class). Also 'custom attribute' seems to be applicable by all
 *   exporters in the same manner (it is custom only because it is unknown at compile time e.g.). extension on the other hand
 *   is just a 'suggestion' which may not be applicable for exporter in all available contexts, but this fact should not break compatibility between model
 *   and exporter.
 *
 *   @author Wojciech Mąka
 */
data class Table<T> internal constructor(
    val name: String? = "untitled",
    val firstRow: Int? = 0,
    val firstColumn: Int? = 0,
    val columns: List<ColumnDef<T>> = emptyList(),
    val rows: List<RowDef<T>>?,
    val tableAttributes: Set<TableAttribute>?,
    val cellAttributes: Set<CellAttribute>?,
) {
    private var indexedCustomRows: Map<RowIndexDef, List<RowDef<T>>>? = null

    init {
        indexedCustomRows = rows?.filter { it.qualifier.createAt != null }
            ?.sortedBy { it.qualifier.createAt!! }
            ?.groupBy { it.qualifier.createAt!! }
    }

    fun forEachColumn(consumer: Consumer<in ColumnDef<T>>) = columns.forEach(consumer)

    fun forEachColumn(consumer: (Int, ColumnDef<T>) -> Unit) = columns.forEachIndexed(consumer)

    fun <E> mapColumns(consumer: (Int, ColumnDef<T>) -> E) = columns.mapIndexed(consumer)

    fun forEachRow(consumer: Consumer<in RowDef<T>>) = rows?.forEach(consumer)

    fun getRowsAt(index: RowIndex): List<RowDef<T>>? {
        return if (index.labels.isEmpty()) {
            indexedCustomRows?.get(RowIndexDef(index.rowIndex))
        } else {
            index.labels.mapNotNull {
                indexedCustomRows?.get(RowIndexDef(index = it.value.index, offsetLabel = it.key))
            }.flatten()
        }
    }

    fun hasRowsAt(index: RowIndex): Boolean = !getRowsAt(index).isNullOrEmpty()

    fun getNextCustomRowIndex(index: RowIndex): RowIndexDef? {
        return indexedCustomRows?.entries
            ?.firstOrNull { it.key.index > index.rowIndex }
            ?.key
    }

    fun getRows(sourceRow: SourceRow<T>): Set<RowDef<T>> {
        val customRows = getRowsAt(sourceRow.rowIndex)?.toSet()
        val matchingRows = rows?.filter { it.isApplicable(sourceRow) }?.toSet()
        return customRows?.let { matchingRows?.plus(it) ?: it } ?: matchingRows ?: emptySet()
    }

    fun hasCustomRows(sourceRow: SourceRow<T>): Boolean {
        return hasRowsAt(sourceRow.rowIndex) || rows?.any { it.shouldInsertRow(sourceRow) } ?: false
    }

    companion object {
        @JvmStatic
        fun <T> builder() = TableBuilder<T>(TableBuilderBase())
    }
}
