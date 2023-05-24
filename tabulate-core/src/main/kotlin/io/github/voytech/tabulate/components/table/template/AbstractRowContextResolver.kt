package io.github.voytech.tabulate.components.table.template

import io.github.voytech.tabulate.Either
import io.github.voytech.tabulate.components.table.model.*
import io.github.voytech.tabulate.components.table.operation.*
import io.github.voytech.tabulate.core.model.AttributedModelOrPart
import io.github.voytech.tabulate.core.model.Attributes
import io.github.voytech.tabulate.core.model.StateAttributes
import io.github.voytech.tabulate.core.model.orEmpty
import io.github.voytech.tabulate.core.layout.CrossedAxis
import io.github.voytech.tabulate.core.operation.*
import io.github.voytech.tabulate.core.operation.RenderingSkipped as OpOverflowResult

internal fun <T : AttributedModelOrPart> T.attributesForAllContexts(): AttributesByContexts<T> =
    distributeAttributesForContexts(
        CellRenderable::class.java,
        RowStartRenderable::class.java,
        RowEndRenderable::class.java,
        ColumnStartRenderable::class.java,
        ColumnEndRenderable::class.java
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

internal fun interface ProvideCellContext<T : Any> : (SyntheticRow<T>, ColumnDef<T>) -> CellRenderable?

internal class SyntheticRow<T : Any>(
    internal val table: Table<T>,
    private val rowDefinitions: Set<RowDef<T>>,
    // computed intermediate properties:
    private val tableAttributesForContext: AttributesByContexts<Table<T>> = table.attributesForAllContexts(),
    internal val cellDefinitions: Map<ColumnKey<T>, CellDef<T>> = rowDefinitions.mergeCells(),
    private val allRowAttributes: Attributes = rowDefinitions.flattenRowAttributes(),
    // attributes to be accessible on AttributedContext:
    internal val rowStartAttributes: Attributes = tableAttributesForContext.get<RowStartRenderable>() + allRowAttributes.forContext<RowStartRenderable>(),
    internal val rowEndAttributes: Attributes = tableAttributesForContext.get<RowEndRenderable<T>>() + allRowAttributes.forContext<RowEndRenderable<T>>(),
    private val rowCellAttributes: Attributes = rowDefinitions.flattenCellAttributes().forContext<CellRenderable>(),
    internal val cellContextAttributes: MutableMap<ColumnDef<T>, Attributes> = mutableMapOf(),
) {

    @Suppress("NOTHING_TO_INLINE")
    private inline fun mergeCellAttributes(column: ColumnDef<T>): Attributes =
        tableAttributesForContext.get<CellRenderable>().orEmpty() +
                column.attributes.orEmpty().forContext<CellRenderable>() +
                rowCellAttributes +
                cellDefinitions[column.id]?.attributes.orEmpty().forContext<CellRenderable>()

    internal fun mapEachCell(
        block: ProvideCellContext<T>, operation: (CellRenderable) -> Boolean
    ): Either<Map<ColumnKey<T>, CellRenderable>, Boolean> {
        var test = false
        val cells = if (cellContextAttributes.isEmpty()) {
            table.columns.mapNotNull { column ->
                cellContextAttributes[column] = mergeCellAttributes(column)
                block(this, column)?.let { column.id to it }?.also { if (operation(it.second)) test = true }
            }.toMap()
        } else {
            cellContextAttributes.keys.mapNotNull { column ->
                block(this, column)?.let { column.id to it }?.also { if (operation(it.second)) test = true }
            }.toMap()
        }
        return if (test) Either.Right(true) else Either.Left(cells)
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
    fun onCellResolved(cell: CellRenderable): RenderingResult
    fun onRowStartResolved(row: RowStartRenderable): RenderingResult
    fun onRowEndResolved(row: RowEndRenderable<T>): RenderingResult
}

/**
 * Given requested index, [Table] model, and map of custom attributes, it resolves [RowEndRenderable] (row context) with
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
    private val continuations: TableContinuations,
    private val listener: CaptureRowCompletion<T>? = null,
) : IndexedContextResolver<RowEndRenderable<T>> {

    private val indexedTableRows: IndexedTableRows<T> = tableModel.indexRows()
    private val rows = QualifiedRows(indexedTableRows)

    protected fun RowStartRenderable.render(): RenderingResult? =
        listener?.onRowStartResolved(this)

    protected fun RowEndRenderable<T>.render(): RenderingResult? =
        listener?.onRowEndResolved(this)

    private fun CellRenderable.render(): RenderingResult? =
        listener?.onCellResolved(this)

    private fun offsetAwareCellContext(sourceRow: SourceRow<T>) = ProvideCellContext { row, column ->
        if (continuations.isValid(column.index)) {
            row.createCellContext(row = sourceRow, column = column, state.data)
        } else null
    }

    private fun orchestrateTableRow(
        tableRowIndex: RowIndex,
        record: IndexedValue<T>? = null,
    ): ContextResult<RowEndRenderable<T>> {
        return SourceRow(tableRowIndex, record?.index, record?.value).let { sourceRow ->
            with(rows.findQualifying(sourceRow)) {
                val provideCell = offsetAwareCellContext(sourceRow)
                createRowStart(rowIndex = tableRowIndex.value, customAttributes = state.data).let { rowStart ->
                    when (val status = rowStart.render()?.status) {
                        Ok -> when (val maybeCells = mapEachCell(provideCell) { cell ->
                            cell.render()?.status.let { it is OpOverflowResult && it.isSkipped(CrossedAxis.Y) }
                        }) {
                            is Either.Left -> tryRenderRowEnd(rowStart, maybeCells.value)
                            is Either.Right -> OverflowResult(OpOverflowResult(CrossedAxis.Y))
                        }

                        else -> OverflowResult(status as InterruptionOnAxis)
                    }
                }
            }
        }
    }

    private fun SyntheticRow<T>.tryRenderRowEnd(
        rowStart: RowStartRenderable,
        cells: Map<ColumnKey<T>, CellRenderable>
    ): ContextResult<RowEndRenderable<T>> =
        createRowEnd(rowStart, cells).let { rowEnd ->
            rowEnd.render().let { result ->
                when (result?.status) {
                    null, Ok -> SuccessResult(rowEnd)
                    else -> OverflowResult(result as OpOverflowResult)
                }
            }
        }

    private fun resolveRowContext(
        requestedIndex: RowIndex,
        indexedRecord: IndexedValue<T>? = null,
    ): IndexedResult<RowEndRenderable<T>> =
        IndexedResult(requestedIndex, indexedRecord?.index, orchestrateTableRow(requestedIndex, indexedRecord))


    /**
     * Resolves indexed [RowEndRenderable]. Index may be equal to parameter index value, or if there are no matching predicates,
     * it may be next matching index or eventually null when no row can be resolved.
     * @param requestedIndex [RowIndex] - index requested by row iterator.
     * @author Wojciech Mąka
     * @since 0.1.0
     */
    override fun resolve(requestedIndex: RowIndex): IndexedResult<RowEndRenderable<T>>? {
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
