package io.github.voytech.tabulate.performance

import io.github.voytech.tabulate.components.table.model.RowDef
import io.github.voytech.tabulate.components.table.model.SourceRow
import io.github.voytech.tabulate.components.table.model.Table
import io.github.voytech.tabulate.components.table.operation.RowEnd
import io.github.voytech.tabulate.components.table.operation.createRowEnd
import io.github.voytech.tabulate.components.table.operation.createCellContext
import io.github.voytech.tabulate.components.table.operation.createRowStart
import io.github.voytech.tabulate.components.table.template.*
import io.github.voytech.tabulate.core.model.StateAttributes

internal class SlowRowResolver<T: Any>(
    private val tableModel: Table<T>,
    private val customAttributes: MutableMap<String, Any>,
    offsets: TableContinuations = TableContinuations(),
    listener: CaptureRowCompletion<T>? = null
): AbstractRowContextResolver<T>(tableModel, StateAttributes(customAttributes), offsets, listener) {

    private val customRows = tableModel.rows?.filter { it.qualifier.index != null }
    private val rowsWithPredicates = tableModel.rows?.filter { it.qualifier.matching != null }

    private fun getRowsAt(index: RowIndex): List<RowDef<T>>? {
        return customRows
            ?.filter { it.shouldInsertRow(SourceRow(index)) }
    }

    private fun hasRowsAt(index: RowIndex): Boolean = !getRowsAt(index).isNullOrEmpty()

    private fun getRows(sourceRow: SourceRow<T>): Set<RowDef<T>> {
        val customRows = getRowsAt(sourceRow.rowIndex)?.toSet()
        val matchingRows = rowsWithPredicates?.filter { it.shouldApplyWhen(sourceRow) }?.toSet()
        return customRows?.let { matchingRows?.plus(it) ?: it } ?: matchingRows ?: emptySet()
    }

    private fun hasCustomRows(sourceRow: SourceRow<T>): Boolean {
        return hasRowsAt(sourceRow.rowIndex)
    }

    private fun resolveAttributedRow(
        tableRowIndex: RowIndex,
        record: IndexedValue<T>? = null
    ): ContextResult<RowEnd<T>> {
        return SourceRow(tableRowIndex, record?.index, record?.value).let { sourceRow ->
            val rowDefinitions = getRows(sourceRow)
            with(SyntheticRow(tableModel, rowDefinitions)) {
                SuccessResult(createRowEnd(
                    createRowStart(rowIndex = tableRowIndex.value, customAttributes = customAttributes).also { it.render() },
                    mapEachCell { row, column ->
                        row.createCellContext(row = sourceRow, column = column, customAttributes)?.also { it.render() }
                    }
                ).also { it.render() })
            }
        }
    }

    override fun resolve(requestedIndex: RowIndex): IndexedResult<RowEnd<T>>? {
        return if (hasCustomRows(SourceRow(requestedIndex))) {
            IndexedResult(requestedIndex, null, resolveAttributedRow(requestedIndex))
        } else null
    }

    override fun getNextRecord(): IndexedValue<T>? {
        throw error("Not used for test purposes - only custom records tested")
    }
}