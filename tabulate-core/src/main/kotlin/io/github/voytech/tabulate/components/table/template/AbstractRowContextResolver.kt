package io.github.voytech.tabulate.components.table.template

import io.github.voytech.tabulate.components.table.model.*
import io.github.voytech.tabulate.components.table.operation.*
import io.github.voytech.tabulate.core.model.AttributedModelOrPart
import io.github.voytech.tabulate.core.model.Attributes
import io.github.voytech.tabulate.core.model.StateAttributes
import io.github.voytech.tabulate.core.model.orEmpty
import io.github.voytech.tabulate.core.template.operation.*

internal fun <T : AttributedModelOrPart<T>> T.attributesForAllContexts(): AttributesByContexts<T> =
    distributeAttributesForContexts(
        CellContext::class.java,
        RowStart::class.java,
        RowEnd::class.java,
        ColumnStart::class.java,
        ColumnEnd::class.java
    )

internal class IndexedTableRows<T : Any>(
    val table: Table<T>,
    private val stepClass: Class<out Enum<*>> = AdditionalSteps::class.java,
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

internal fun <T : Any> Table<T>.indexRows(): IndexedTableRows<T> = IndexedTableRows(this)

internal fun interface ProvideCellContext<T : Any> : (SyntheticRow<T>, ColumnDef<T>) -> CellContext?

internal class SyntheticRow<T : Any>(
    internal val table: Table<T>,
    private val rowDefinitions: Set<RowDef<T>>,
    // computed intermediate properties:
    private val tableAttributesForContext: AttributesByContexts<Table<T>> = table.attributesForAllContexts(),
    internal val cellDefinitions: Map<ColumnKey<T>, CellDef<T>> = rowDefinitions.mergeCells(),
    private val allRowAttributes: Attributes = rowDefinitions.flattenRowAttributes(),
    // attributes to be accessible on AttributedContext:
    internal val rowStartAttributes: Attributes = tableAttributesForContext.get<RowStart>() + allRowAttributes.forContext<RowStart>(),
    internal val rowEndAttributes: Attributes = tableAttributesForContext.get<RowEnd<T>>() + allRowAttributes.forContext<RowEnd<T>>(),
    private val rowCellAttributes: Attributes = rowDefinitions.flattenCellAttributes().forContext<CellContext>(),
    internal val cellContextAttributes: MutableMap<ColumnDef<T>, Attributes> = mutableMapOf(),
) {

    @Suppress("NOTHING_TO_INLINE")
    private inline fun mergeCellAttributes(column: ColumnDef<T>): Attributes =
        tableAttributesForContext.get<CellContext>().orEmpty() +
                column.attributes.orEmpty().forContext<CellContext>() +
                rowCellAttributes +
                cellDefinitions[column.id]?.attributes.orEmpty().forContext<CellContext>()

    internal fun mapEachCell(
        block: ProvideCellContext<T>,
    ): Map<ColumnKey<T>, CellContext> =
        if (cellContextAttributes.isEmpty()) {
            table.columns.mapNotNull { column ->
                cellContextAttributes[column] = mergeCellAttributes(column)
                block(this, column)?.let { column.id to it }
            }.toMap()
        } else {
            cellContextAttributes.keys.mapNotNull { column ->
                block(this, column)?.let { column.id to it }
            }.toMap()
        }
}

internal class QualifiedRows<T : Any>(private val indexedTableRows: IndexedTableRows<T>) {
    private var qualifiedRowsIndex: MutableMap<Set<RowDef<T>>, SyntheticRow<T>> = mutableMapOf()

    internal fun findQualifying(sourceRow: SourceRow<T>): SyntheticRow<T> =
        indexedTableRows.getRows(sourceRow).let { matchedRowDefs ->
            qualifiedRowsIndex.computeIfAbsent(matchedRowDefs) {
                SyntheticRow(indexedTableRows.table, it)
            }
        }
}

interface CaptureRowCompletion<T> {
    fun onCellResolved(cell: CellContext): OperationResult?
    fun onRowStartResolved(row: RowStart): OperationResult?
    fun onRowEndResolved(row: RowEnd<T>): OperationResult?
}

/**
 * Given requested index, [Table] model, and map of custom attributes, it resolves [RowEnd] (row context) with
 * associated effective index.
 *
 * Additionally, it notifies about following events:
 *  - all row attributes on row has been resolved,
 *  - cell and its attributes has been resolved,
 *  - entire row has been completed (row with attributes and all row cells with its attributes).
 * @author Wojciech Mąka
 * @since 0.1.0
 */
internal abstract class AbstractRowContextResolver<T : Any>(
    tableModel: Table<T>,
    private val state: StateAttributes,
    private val offsets: OverflowOffsets,
    private val listener: CaptureRowCompletion<T>? = null,
) : IndexedContextResolver<RowEnd<T>> {

    private val indexedTableRows: IndexedTableRows<T> = tableModel.indexRows()
    private val rows = QualifiedRows(indexedTableRows)

    @Suppress("NOTHING_TO_INLINE")
    protected inline fun RowStart.render(): OperationResult? =
        listener?.onRowStartResolved(this)

    @Suppress("NOTHING_TO_INLINE")
    protected inline fun RowEnd<T>.render(): OperationResult? =
        listener?.onRowEndResolved(this)

    @Suppress("NOTHING_TO_INLINE")
    protected inline fun CellContext.render(): CellContext =
        also { listener?.onCellResolved(it) }

    private fun offsetAwareCellContext(sourceRow: SourceRow<T>) = ProvideCellContext { row, column ->
        if (offsets.isValid(column.index)) {
            row.createCellContext(row = sourceRow, column = column, state.data)?.render()
        } else null
    }

    private fun orchestrateTableRow(
        tableRowIndex: RowIndex,
        record: IndexedValue<T>? = null,
    ): ContextResult<RowEnd<T>> {
        return SourceRow(tableRowIndex, record?.index, record?.value).let { sourceRow ->
            with(rows.findQualifying(sourceRow)) {
                val provideCell = offsetAwareCellContext(sourceRow)
                createRowStart(rowIndex = tableRowIndex.value, customAttributes = state.data).let { rowStart ->
                    when (val status = rowStart.render()) {
                        Success -> tryRenderRowEnd(rowStart,mapEachCell(provideCell))
                        else -> OverflowResult(status as io.github.voytech.tabulate.core.template.operation.OverflowResult)
                    }
                }
            }
        }
    }

    private fun SyntheticRow<T>.tryRenderRowEnd(rowStart: RowStart,cells: Map<ColumnKey<T>,CellContext>): ContextResult<RowEnd<T>> =
        createRowEnd(rowStart, cells).let { rowEnd ->
            rowEnd.render().let { result ->
                when(result) {
                    null, Success -> SuccessResult(rowEnd)
                    else -> OverflowResult(result as io.github.voytech.tabulate.core.template.operation.OverflowResult)
                }
            }
        }

    private fun resolveRowContext(
        requestedIndex: RowIndex,
        indexedRecord: IndexedValue<T>? = null,
    ): IndexedResult<RowEnd<T>> =
        IndexedResult(requestedIndex, indexedRecord?.index, orchestrateTableRow(requestedIndex, indexedRecord))


    /**
     * Resolves indexed [RowEnd]. Index may be equal to parameter index value, or if there are no matching predicates,
     * it may be next matching index or eventually null when no row can be resolved.
     * @param requestedIndex [RowIndex] - index requested by row iterator.
     * @author Wojciech Mąka
     * @since 0.1.0
     */
    override fun resolve(requestedIndex: RowIndex): IndexedResult<RowEnd<T>>? {
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