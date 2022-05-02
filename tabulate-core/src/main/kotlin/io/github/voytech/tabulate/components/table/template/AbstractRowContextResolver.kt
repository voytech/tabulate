package io.github.voytech.tabulate.components.table.template

import io.github.voytech.tabulate.components.table.model.*
import io.github.voytech.tabulate.components.table.model.attributes.CellAttribute
import io.github.voytech.tabulate.components.table.model.attributes.RowAttribute
import io.github.voytech.tabulate.components.table.operation.*
import io.github.voytech.tabulate.core.model.Attributes
import io.github.voytech.tabulate.core.model.orEmpty

internal class IndexedTableRows<T: Any>(
    val table: Table<T>,
    private val stepClass: Class<out Enum<*>> = AdditionalSteps::class.java
) {
    private val indexedCustomRows: Map<RowIndexDef, List<RowDef<T>>>? = table.rows
        ?.filter { it.qualifier.index != null }
        ?.map { it.qualifier.index?.materialize() to it }
        ?.flatMap { it.first?.map { index -> index to it.second } ?: emptyList() }
        ?.groupBy({ it.first }, { it.second })
        ?.toSortedMap()

    private val rowsWithPredicates = table.rows?.filter { it.qualifier.matching != null }

    private fun parseStep(step: String): Enum<*> = stepClass.enumConstants.find { step == it.name }
        ?: throw error("Could not resolve step enum")

    @JvmSynthetic
    internal fun getRowsAt(index: RowIndex): List<RowDef<T>>? {
        return indexedCustomRows?.get(
            index.step?.let { RowIndexDef(index = it.index, step = parseStep(it.step)) } ?: RowIndexDef(index.value)
        )
    }

    private fun hasRowsAt(index: RowIndex): Boolean = !getRowsAt(index).isNullOrEmpty()

    @JvmSynthetic
    internal fun getNextCustomRowIndex(index: RowIndex): RowIndexDef? {
        return indexedCustomRows?.entries
            ?.firstOrNull { it.key > index.asRowIndexDef(stepClass) }
            ?.key
    }

    @JvmSynthetic
    internal fun getRows(sourceRow: SourceRow<T>): Set<RowDef<T>> {
        val customRows = getRowsAt(sourceRow.rowIndex)?.toSet()
        val matchingRows = rowsWithPredicates?.filter { it.shouldApplyWhen(sourceRow) }?.toSet()
        return customRows?.let { matchingRows?.plus(it) ?: it } ?: matchingRows ?: emptySet()
    }

    @JvmSynthetic
    internal fun hasCustomRows(sourceRow: SourceRow<T>): Boolean {
        return hasRowsAt(sourceRow.rowIndex)
    }
}

internal fun <T: Any> Table<T>.indexRows(): IndexedTableRows<T> = IndexedTableRows(this)

internal class SyntheticRow<T: Any>(
    internal val table: Table<T>,
    private val rowDefinitions: Set<RowDef<T>>,
    internal val cellDefinitions: Map<ColumnKey<T>, CellDef<T>> = rowDefinitions.mergeCells(),
    private val rowCellAttributes: Attributes<CellAttribute<*>> = rowDefinitions.flattenCellAttributes(),
    internal val rowAttributes: Attributes<RowAttribute<*>> =
        table.rowAttributes.orEmpty() + rowDefinitions.flattenRowAttributes(),
    internal val cellAttributes: MutableMap<ColumnDef<T>, Attributes<CellAttribute<*>>> = mutableMapOf()
) {

    @Suppress("NOTHING_TO_INLINE")
    inline fun mergeCellAttributes(column: ColumnDef<T>) =
            table.cellAttributes.orEmpty() +
            column.cellAttributes.orEmpty() +
            rowCellAttributes +
            cellDefinitions[column.id]?.cellAttributes.orEmpty()


    internal fun mapEachCell(
        block: (syntheticRow: SyntheticRow<T>, column: ColumnDef<T>) -> CellContext?
    ): Map<ColumnKey<T>, CellContext> =
        if (cellAttributes.isEmpty()) {
            table.columns.mapNotNull { column ->
                cellAttributes[column] = mergeCellAttributes(column)
                block(this, column)?.let { column.id to it }
            }.toMap()
        } else {
            cellAttributes.keys.mapNotNull { column ->
                block(this, column)?.let { column.id to it }
            }.toMap()
        }
}

internal class QualifiedRows<T: Any>(private val indexedTableRows: IndexedTableRows<T>) {
    private var qualifiedRowsIndex: MutableMap<Set<RowDef<T>>, SyntheticRow<T>> = mutableMapOf()

    internal fun findQualifying(sourceRow: SourceRow<T>): SyntheticRow<T> =
        indexedTableRows.getRows(sourceRow).let { matchedRowDefs ->
            qualifiedRowsIndex.computeIfAbsent(matchedRowDefs) {
                SyntheticRow(indexedTableRows.table, it)
            }
        }
}

interface RowCompletionListener<T> {
    fun onAttributedCellResolved(cell: CellContext)
    fun onAttributedRowResolved(row: RowStart)
    fun onAttributedRowResolved(row: RowEnd<T>)
}

/**
 * Given requested index, [Table] model, and map of custom attributes, it resolves [RowEnd] (row context) with
 * associated effective index.
 *
 * Additionally it notifies about following events:
 *  - all row attributes on row has been resolved,
 *  - cell and its attributes has been resolved,
 *  - entire row has been completed (row with attributes and all row cells with its attributes).
 * @author Wojciech Mąka
 * @since 0.1.0
 */
internal abstract class AbstractRowContextResolver<T: Any>(
    tableModel: Table<T>,
    private val customAttributes: MutableMap<String, Any>,
    private val listener: RowCompletionListener<T>? = null,
) : IndexedContextResolver<T, RowEnd<T>> {

    private val indexedTableRows: IndexedTableRows<T> = tableModel.indexRows()
    private val rows = QualifiedRows(indexedTableRows)

    @Suppress("NOTHING_TO_INLINE")
    protected inline fun RowStart.notify(): RowStart =
        also { listener?.onAttributedRowResolved(it) }

    @Suppress("NOTHING_TO_INLINE")
    protected inline fun RowEnd<T>.notify(): RowEnd<T> =
        also { listener?.onAttributedRowResolved(it) }

    @Suppress("NOTHING_TO_INLINE")
    protected inline fun CellContext.notify(): CellContext =
        also { listener?.onAttributedCellResolved(it) }


    private fun resolveAttributedRow(
        tableRowIndex: RowIndex,
        record: IndexedValue<T>? = null
    ): RowEnd<T> {
        return SourceRow(tableRowIndex, record?.index, record?.value).let { sourceRow ->
            with(rows.findQualifying(sourceRow)) {
                asRowStart(rowIndex = tableRowIndex.value, customAttributes = customAttributes).notify()
                    .let {
                        it.asRowEnd(
                            mapEachCell { row, column ->
                                row.asCellContext(row = sourceRow, column = column, customAttributes)?.notify()
                            }
                        )
                    }.notify()
            }
        }
    }

    private fun resolveRowContext(
        tableRowIndex: RowIndex,
        indexedRecord: IndexedValue<T>? = null,
    ): IndexedContext<RowEnd<T>> {
        return IndexedContext(tableRowIndex, resolveAttributedRow(tableRowIndex, indexedRecord))
    }

    /**
     * Resolves indexed [RowEnd]. Index may be equal to parameter index value, or if there are no matching predicates,
     * it may be next matching index or eventually null when no row can be resolved.
     * @param requestedIndex [RowIndex] - index requested by row iterator.
     * @author Wojciech Mąka
     * @since 0.1.0
     */
    override fun resolve(requestedIndex: RowIndex): IndexedContext<RowEnd<T>>? {
        return if (indexedTableRows.hasCustomRows(SourceRow(requestedIndex))) {
            resolveRowContext(requestedIndex)
        } else {
            getNextRecord().let {
                if (it != null) {
                    resolveRowContext(requestedIndex, it)
                } else {
                    indexedTableRows.getNextCustomRowIndex(requestedIndex)
                        ?.let { nextIndexDef ->
                            resolveRowContext(requestedIndex + nextIndexDef)
                        }
                }
            }
        }
    }

    /**
     * Provides next record from data source. Resolved [IndexedValue] may wrap a value or null if there is no more
     * records left.
     * @author Wojciech Mąka
     * @since 0.1.0
     */
    protected abstract fun getNextRecord(): IndexedValue<T>?
}
