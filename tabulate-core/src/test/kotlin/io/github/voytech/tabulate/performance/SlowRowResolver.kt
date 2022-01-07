package io.github.voytech.tabulate.performance

import io.github.voytech.tabulate.model.RowDef
import io.github.voytech.tabulate.model.SourceRow
import io.github.voytech.tabulate.model.Table
import io.github.voytech.tabulate.template.context.RowIndex
import io.github.voytech.tabulate.template.operations.AttributedRowWithCells
import io.github.voytech.tabulate.template.operations.createAttributedCell
import io.github.voytech.tabulate.template.operations.createAttributedRow
import io.github.voytech.tabulate.template.operations.withCells
import io.github.voytech.tabulate.template.resolvers.AbstractRowContextResolver
import io.github.voytech.tabulate.template.resolvers.IndexedContext
import io.github.voytech.tabulate.template.resolvers.RowCompletionListener
import io.github.voytech.tabulate.template.resolvers.SyntheticRow

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
    ): AttributedRowWithCells<T> {
        return SourceRow(tableRowIndex, record?.index, record?.value).let { sourceRow ->
            val rowDefinitions = getRows(sourceRow)
            with(SyntheticRow(tableModel, rowDefinitions)) {
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

    override fun resolve(requestedIndex: RowIndex): IndexedContext<AttributedRowWithCells<T>>? {
        return if (hasCustomRows(SourceRow(requestedIndex))) {
            IndexedContext(requestedIndex, resolveAttributedRow(requestedIndex))
        } else null
    }

    override fun getNextRecord(): IndexedValue<T>? {
        throw error("Not used for test purposes - only custom records tested")
    }
}