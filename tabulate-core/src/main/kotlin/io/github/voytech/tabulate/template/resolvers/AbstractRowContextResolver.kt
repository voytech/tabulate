package io.github.voytech.tabulate.template.resolvers

import io.github.voytech.tabulate.model.*
import io.github.voytech.tabulate.model.attributes.alias.CellAttribute
import io.github.voytech.tabulate.model.attributes.alias.RowAttribute
import io.github.voytech.tabulate.model.attributes.overrideAttributesLeftToRight
import io.github.voytech.tabulate.template.context.AdditionalSteps
import io.github.voytech.tabulate.template.context.RowIndex
import io.github.voytech.tabulate.template.operations.*

internal class IndexedTableRows<T>(
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

internal fun <T> Table<T>.indexRows(): IndexedTableRows<T> = IndexedTableRows(this)

internal class SyntheticRow<T>(
    internal val table: Table<T>,
    private val rowDefinitions: Set<RowDef<T>>,
    internal val cellDefinitions: Map<ColumnKey<T>, CellDef<T>> = rowDefinitions.mergeCells(),
    private val rowCellAttributes: Set<CellAttribute> = rowDefinitions.flattenCellAttributes(),
    internal val rowAttributes: Set<RowAttribute> = overrideAttributesLeftToRight(
        table.rowAttributes.orEmpty() + rowDefinitions.flattenRowAttributes()
    ),
    internal val cellAttributes: MutableMap<ColumnDef<T>, Set<CellAttribute>> = mutableMapOf()
) {

    @Suppress("NOTHING_TO_INLINE")
    inline fun mergeCellAttributes(column: ColumnDef<T>) =
        overrideAttributesLeftToRight(
            table.cellAttributes.orEmpty() +
                    column.cellAttributes.orEmpty() +
                    rowCellAttributes +
                    (cellDefinitions[column.id]?.cellAttributes).orEmpty()
        )

    internal fun mapEachCell(
        block: (syntheticRow: SyntheticRow<T>, column: ColumnDef<T>) -> AttributedCell?
    ): Map<ColumnKey<T>, AttributedCell> =
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

internal class QualifiedRows<T>(private val indexedTableRows: IndexedTableRows<T>) {
    private var qualifiedRowsIndex: MutableMap<Set<RowDef<T>>, SyntheticRow<T>> = mutableMapOf()

    internal fun findQualifying(sourceRow: SourceRow<T>): SyntheticRow<T> =
        indexedTableRows.getRows(sourceRow).let { matchedRowDefs ->
            qualifiedRowsIndex.computeIfAbsent(matchedRowDefs) {
                SyntheticRow(indexedTableRows.table, it)
            }
        }
}

internal interface RowCompletionListener<T> {
    fun onAttributedCellResolved(cell: AttributedCell)
    fun onAttributedRowResolved(row: AttributedRow)
    fun onAttributedRowResolved(row: AttributedRowWithCells<T>)
}

/**
 * Given requested index, [Table] model, and global custom attributes, it resolves [AttributedRowWithCells] context data with
 * effective index (effective index may differ from requested one if there are no rows matching predicate
 * - in that case - row context with next matching index is returned).
 * Additionally - while resolving - it notifies about:
 *  - computed row attributes on row,
 *  - each computed cell and its attributes,
 *  - entire row completion - that is completion of row with attributes and all row cells with its attributes.
 * @author Wojciech Mąka
 */
internal abstract class AbstractRowContextResolver<T>(
    tableModel: Table<T>,
    private val customAttributes: MutableMap<String, Any>,
    private val listener: RowCompletionListener<T>? = null,
) : IndexedContextResolver<T, AttributedRowWithCells<T>> {

    private val indexedTableRows: IndexedTableRows<T> = tableModel.indexRows()
    private val rows = QualifiedRows(indexedTableRows)

    @Suppress("NOTHING_TO_INLINE")
    protected inline fun AttributedRow.notify(): AttributedRow =
        also { listener?.onAttributedRowResolved(it) }

    @Suppress("NOTHING_TO_INLINE")
    protected inline fun AttributedRowWithCells<T>.notify(): AttributedRowWithCells<T> =
        also { listener?.onAttributedRowResolved(it) }

    @Suppress("NOTHING_TO_INLINE")
    protected inline fun AttributedCell.notify(): AttributedCell =
        also { listener?.onAttributedCellResolved(it) }


    private fun resolveAttributedRow(
        tableRowIndex: RowIndex,
        record: IndexedValue<T>? = null
    ): AttributedRowWithCells<T> {
        return SourceRow(tableRowIndex, record?.index, record?.value).let { sourceRow ->
            with(rows.findQualifying(sourceRow)) {
                createAttributedRow(rowIndex = tableRowIndex.value, customAttributes = customAttributes).notify()
                    .let {
                        it.withCells(
                            mapEachCell { row, column ->
                                row.createAttributedCell(row = sourceRow, column = column, customAttributes)?.notify()
                            }
                        )
                    }.notify()
            }
        }
    }

    private fun resolveRowContext(
        tableRowIndex: RowIndex,
        indexedRecord: IndexedValue<T>? = null,
    ): IndexedContext<AttributedRowWithCells<T>> {
        return IndexedContext(tableRowIndex, resolveAttributedRow(tableRowIndex, indexedRecord))
    }

    /**
     * Resolves indexed [AttributedRowWithCells]. Index may be equal to parameter index value, or if there are no matching predicates,
     * it may be next matching index or eventually null when no row can be resolved.
     * @param requestedIndex [RowIndex] - index requested by row iterator.
     * @author Wojciech Mąka
     */
    override fun resolve(requestedIndex: RowIndex): IndexedContext<AttributedRowWithCells<T>>? {
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
     */
    protected abstract fun getNextRecord(): IndexedValue<T>?
}
