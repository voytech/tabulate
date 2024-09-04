package io.github.voytech.tabulate.components.table.template

import io.github.voytech.tabulate.components.table.model.*
import io.github.voytech.tabulate.components.table.rendering.*
import io.github.voytech.tabulate.components.table.template.TableRenderIterations.Companion.END_RECORD
import io.github.voytech.tabulate.components.table.template.TableRenderIterations.Companion.END_ROW
import io.github.voytech.tabulate.components.table.template.TableRenderIterations.Companion.START_RECORD
import io.github.voytech.tabulate.components.table.template.TableRenderIterations.Companion.START_ROW
import io.github.voytech.tabulate.core.layout.Axis
import io.github.voytech.tabulate.core.model.*
import io.github.voytech.tabulate.core.operation.*
import io.github.voytech.tabulate.core.operation.Nothing
import io.github.voytech.tabulate.core.operation.RenderingSkipped

internal fun <T : AttributedModelOrPart> T.attributesForAllContexts(): AttributesByContexts<T> =
    distributeAttributesForContexts(
        CellRenderableEntity::class.java,
        RowStartRenderableEntity::class.java,
        RowEndRenderableEntity::class.java,
        ColumnStartRenderableEntity::class.java,
        ColumnEndRenderableEntity::class.java
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
    internal fun getNextCustomRowIndexDefinition(index: RowIndex): RowIndexDef? {
        return indexedCustomRows?.entries
            ?.firstOrNull { it.key > index.asRowIndexDef(stepClass) }
            ?.key
    }

    @JvmSynthetic
    internal fun getNextCustomRowIndex(index: RowIndex): RowIndex? =
        getNextCustomRowIndexDefinition(index)?.let { nextIndexDef -> index + nextIndexDef }


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

internal class SyntheticRow<T : Any>(
    internal val table: Table<T>,
    private val rowDefinitions: Set<RowDef<T>>,
    // computed intermediate properties:
    private val tableAttributesForContext: AttributesByContexts<Table<T>> = table.attributesForAllContexts(),
    internal val cellDefinitions: Map<ColumnKey<T>, CellDef<T>> = rowDefinitions.mergeCells(),
    private val allRowAttributes: Attributes = rowDefinitions.flattenRowAttributes(),
    // attributes to be accessible on AttributedContext:
    internal val rowStartAttributes: Attributes = tableAttributesForContext.get<RowStartRenderableEntity>() + allRowAttributes.forContext<RowStartRenderableEntity>(),
    internal val rowEndAttributes: Attributes = tableAttributesForContext.get<RowEndRenderableEntity<T>>() + allRowAttributes.forContext<RowEndRenderableEntity<T>>(),
    private val rowCellAttributes: Attributes = rowDefinitions.flattenCellAttributes()
        .forContext<CellRenderableEntity>(),
    internal val cellContextAttributes: MutableMap<ColumnDef<T>, Attributes> = mutableMapOf(),
) {

    internal fun mergeCellAttributes(column: ColumnDef<T>): Attributes {
        return tableAttributesForContext.get<CellRenderableEntity>().ensure() +
                column.attributes.ensure().forContext<CellRenderableEntity>() +
                rowCellAttributes +
                cellDefinitions[column.id]?.attributes.ensure().forContext<CellRenderableEntity>()
    }

}

internal class QualifiedRows<T : Any>(private val indexedTableRows: IndexedTableRows<T>) {
    private val qualifiedRowsIndex: MutableMap<Set<RowDef<T>>, SyntheticRow<T>> = mutableMapOf()

    internal fun findQualifying(sourceRow: SourceRow<T>): SyntheticRow<T> =
        indexedTableRows.getRows(sourceRow).let { matchedRowDefs ->
            qualifiedRowsIndex.computeIfAbsent(matchedRowDefs) {
                SyntheticRow(indexedTableRows.table, it)
            }
        }
}

/**
 * Given requested index, [Table] model, and map of custom attributes, it resolves [RowEndRenderableEntity] (row context) with
 * associated effective index.
 *
 * Additionally, it notifies about following events:
 *  - all row attributes on row has been resolved,
 *  - cell and its attributes has been resolved,
 *  - entire row has been completed (row with attributes and all row cells with its attributes).
 * @author Wojciech Mąka
 * @since 0.1.0
 */
internal class TableRowsRenderer<T : Any>(
    tableModel: Table<T>,
    dataSourceActiveWindow: Iterable<T>,
    private val api: ExportApi,
    private val lastRecordIndex: Int,
    private val renderIterations: TableRenderIterations,
    private val state: StateAttributes = api.getCustomAttributes()
) : IndexedContextResolver<RowEndRenderableEntity<T>> {

    private val iterator = dataSourceActiveWindow.iterator()
    private var recordIndex = renderIterations.getStartRecordIndex() ?: 0
    private val indexIncrement = MutableRowIndex().apply { renderIterations.getStartRowIndex()?.let { set(it) } }

    private val indexedTableRows: IndexedTableRows<T> = tableModel.indexRows()
    private val rows = QualifiedRows(indexedTableRows)

    data class CellRenderingResult<T>(
        val result: RenderingResult,
        val key: ColumnKey<T>,
        val cell: CellRenderableEntity
    )

    private fun List<CellRenderingResult<T>>.firstNokResultOrNull() = firstOrNull { it.result.hasOverflown() }


    private fun List<CellRenderingResult<T>>.asMap(): Map<ColumnKey<T>, CellRenderableEntity> =
        associate { it.key to it.cell }

    private fun SyntheticRow<T>.resolveAndRenderCell(
        sourceRow: SourceRow<T>, column: ColumnDef<T>
    ): CellRenderingResult<T>? =
        if (renderIterations.inRenderWindow(column.index)) {
            createCellContext(sourceRow, column, state.data)?.let {
                CellRenderingResult(api.renderOrMeasure(it), column.id, it)
            }
        } else null

    private fun SyntheticRow<T>.resolveAndRenderCells(sourceRow: SourceRow<T>): List<CellRenderingResult<T>> =
        if (cellContextAttributes.isEmpty()) {
            table.columns.mapNotNull { column ->
                cellContextAttributes[column] = mergeCellAttributes(column)
                resolveAndRenderCell(sourceRow, column)
            }
        } else {
            cellContextAttributes.keys.mapNotNull { column ->
                resolveAndRenderCell(sourceRow, column)
            }
        }

    private fun SyntheticRow<T>.resolveAndRenderRowEnd(
        rowStart: RowStartRenderableEntity,
        cells: Map<ColumnKey<T>, CellRenderableEntity>
    ): ContextResult<RowEndRenderableEntity<T>> =
        createRowEnd(rowStart, cells).let { rowEnd ->
            api.renderOrMeasure(rowEnd).let { result ->
                when (result.status) {
                    Ok, Nothing -> SuccessResult(rowEnd)
                    else -> OverflowResult(result.status as AxisBoundStatus)
                }
            }
        }

    private fun resolveAndRenderRow(
        rowIndex: RowIndex, record: IndexedValue<T>? = null,
    ): ContextResult<RowEndRenderableEntity<T>> {
        return SourceRow(rowIndex, record?.index, record?.value).let { sourceRow ->
            with(rows.findQualifying(sourceRow)) {
                createRowStart(rowIndex = rowIndex.value, customAttributes = state.data).let { rowStart ->
                    when (val status = api.renderOrMeasure(rowStart).status) {
                        Ok, Nothing, is RenderingClipped -> resolveAndRenderCells(sourceRow).let {
                            it.firstNokResultOrNull()?.let { nok ->
                                OverflowResult(nok.result.status as AxisBoundStatus)
                                //@TODO I think when clipped then we should continue with resolveAndRenderRowEnd
                            } ?: run {
                                resolveAndRenderRowEnd(rowStart, it.asMap())
                            }
                        }

                        else -> OverflowResult(status as AxisBoundStatus)
                    }
                }
            }
        }
    }

    private fun resolveAndRenderRowAtIndex(
        requestedIndex: RowIndex,
        indexedRecord: IndexedValue<T>? = null,
    ): IndexedResult<RowEndRenderableEntity<T>> =
        IndexedResult(requestedIndex, indexedRecord?.index, resolveAndRenderRow(requestedIndex, indexedRecord))


    /**
     * Resolves indexed [RowEndRenderableEntity]. Index may be equal to parameter index value, or if there are no matching predicates,
     * it may be next matching index or eventually null when no row can be resolved.
     * @param requestedIndex [RowIndex] - index requested by row iterator.
     * @author Wojciech Mąka
     * @since 0.1.0
     */
    override fun resolve(requestedIndex: RowIndex): IndexedResult<RowEndRenderableEntity<T>>? {
        val maxIndex = renderIterations.getEndRowIndex()
        return if (indexedTableRows.hasCustomRows(SourceRow(requestedIndex))) {
            resolveAndRenderRowAtIndex(requestedIndex)
        } else {
            getNextRecord().let {
                if (it != null) {
                    resolveAndRenderRowAtIndex(requestedIndex, it)
                } else {
                    indexedTableRows.getNextCustomRowIndex(requestedIndex)
                        ?.let { nextRowIndex ->
                            if (maxIndex != null && nextRowIndex.value > maxIndex.value) return null
                            resolveAndRenderRowAtIndex(nextRowIndex)
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
    private fun getNextRecord(): IndexedValue<T>? = if (iterator.hasNext()) {
        IndexedValue(recordIndex++, iterator.next())
    } else null

    /**
     * @author Wojciech Mąka
     */
    private fun IndexedResult<RowEndRenderableEntity<T>>.startWithSkippedRowOnNextIteration(processedCnt: Int) {
        if (processedCnt > 1) {
            // This means whe have processed at least one row so there is something to render something on this iteration
            val finalRecordIndex = sourceRecordIndex ?: recordIndex
            api.iterations().appendAttributes(END_ROW to rowIndex - 1, END_RECORD to finalRecordIndex - 1)
            api.iterations().appendPending(START_ROW to rowIndex, START_RECORD to finalRecordIndex)
        } else {
            // This mean there was nothing to render on this iteration, so we can retry to absorb new space.
            api.iterations().retryIteration()
        }
    }

    private fun IndexedResult<RowEndRenderableEntity<T>>.startWithNextExistingRowOnNextIteration() {
        val nextDefinedRowIndex = indexedTableRows.getNextCustomRowIndex(rowIndex)
        val nextRowIndex = rowIndex.inc()
        if (nextDefinedRowIndex != null || recordIndex <= lastRecordIndex) {
            api.iterations().appendAttributes(END_ROW to nextRowIndex - 1, END_RECORD to recordIndex - 1)
            api.iterations().appendPending(START_ROW to nextRowIndex, START_RECORD to recordIndex)
        }
    }

    private fun RowIndex.inActiveWindow(): Boolean {
        val maxIndex = api.iterations().getCurrentIterationAttributeOrNull<RowIndex>(END_ROW)
        val minIndex = api.iterations().getCurrentIterationAttributeOrNull<RowIndex>(START_ROW)
        return !(maxIndex != null && value > maxIndex.value || minIndex != null && value < minIndex.value)
    }

    fun renderRows(then: () -> RenderingResult): RenderingResult {
        var processedCnt = 0
        while (indexIncrement.getRowIndex().inActiveWindow()) {
            val row = resolve(indexIncrement.getRowIndex())
            if (row != null) {
                processedCnt++
                when (row.result) {
                    is SuccessResult -> {
                        indexIncrement.set(row.rowIndex)
                        indexIncrement.inc()
                    }

                    is OverflowResult -> {
                        when (row.result.overflow) {
                            is RenderingSkipped -> {
                                row.startWithSkippedRowOnNextIteration(processedCnt)
                                break
                            }

                            is RenderingClipped -> {
                                row.startWithNextExistingRowOnNextIteration()
                                break
                            }
                        }
                    }
                }
            } else break
        }
        then()
        return Ok.asResult()
    }

}
