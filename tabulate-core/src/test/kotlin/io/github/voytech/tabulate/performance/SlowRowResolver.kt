package io.github.voytech.tabulate.performance

import io.github.voytech.tabulate.components.table.model.RowDef
import io.github.voytech.tabulate.components.table.model.SourceRow
import io.github.voytech.tabulate.components.table.model.Table
import io.github.voytech.tabulate.components.table.operation.RowClosingContext
import io.github.voytech.tabulate.components.table.operation.asRowClosing
import io.github.voytech.tabulate.components.table.operation.createCellContext
import io.github.voytech.tabulate.components.table.operation.createRowOpening
import io.github.voytech.tabulate.components.table.template.*

internal class SlowRowResolver<T>(
    private val tableModel: Table<T>,
    private val customAttributes: MutableMap<String, Any>,
    listener: RowCompletionListener<T>? = null
): AbstractRowContextResolver<T>(tableModel, customAttributes, listener) {

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
    ): RowClosingContext<T> {
        return SourceRow(tableRowIndex, record?.index, record?.value).let { sourceRow ->
            val rowDefinitions = getRows(sourceRow)
            with(SyntheticRow(tableModel, rowDefinitions)) {
                createRowOpening(rowIndex = tableRowIndex.value, customAttributes = customAttributes).notify()
                    .let {
                        it.asRowClosing(
                            mapEachCell { row, column ->
                                row.createCellContext(row = sourceRow, column = column, customAttributes)?.notify()
                            }
                        )
                    }.notify()
            }
        }
    }

    override fun resolve(requestedIndex: RowIndex): IndexedContext<RowClosingContext<T>>? {
        return if (hasCustomRows(SourceRow(requestedIndex))) {
            IndexedContext(requestedIndex, resolveAttributedRow(requestedIndex))
        } else null
    }

    override fun getNextRecord(): IndexedValue<T>? {
        throw error("Not used for test purposes - only custom records tested")
    }
}