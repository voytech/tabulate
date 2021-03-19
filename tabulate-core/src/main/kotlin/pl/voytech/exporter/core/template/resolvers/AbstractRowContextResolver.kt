package pl.voytech.exporter.core.template.resolvers

import pl.voytech.exporter.core.model.*
import pl.voytech.exporter.core.model.attributes.CellAttribute
import pl.voytech.exporter.core.model.attributes.RowAttribute
import pl.voytech.exporter.core.model.attributes.mergeAttributes
import pl.voytech.exporter.core.model.attributes.mergeLatterWins
import pl.voytech.exporter.core.template.context.AttributedRow
import pl.voytech.exporter.core.template.context.GlobalContextAndAttributes

abstract class AbstractRowContextResolver<DS, T>(
    tableModel: Table<T>,
    stateAndAttributes: GlobalContextAndAttributes<T>
) :
    TableDataSourceContextResolver<DS, T>(tableModel, stateAndAttributes) {

    private fun computeCells(rowDefinitions: Set<Row<T>>): Map<ColumnKey<T>, Cell<T>> {
        return rowDefinitions.mapNotNull { row -> row.cells }.fold(mapOf(), { acc, m -> acc + m })
    }

    private fun computeRowLevelCellAttributes(rowDefinitions: Set<Row<T>>): Set<CellAttribute<*>> {
        return mergeLatterWins(*(rowDefinitions.mapNotNull { i -> i.cellAttributes }.toTypedArray()))
    }

    private fun computeRowAttributes(rowDefinitions: Set<Row<T>>): Set<RowAttribute> {
        return rowDefinitions.mapNotNull { attribs -> attribs.rowAttributes }
            .fold(setOf(), { acc, r -> acc + r })
    }

    private fun resolveAttributedRow(tableRowIndex: Int, record: IndexedValue<T>? = null): AttributedRow<T> {
        return SourceRow(
            rowIndex = tableRowIndex,
            objectIndex = record?.index,
            record = record?.value
        ).let { sourceRow ->
            val rowDefinitions = tableModel.getRowsFor(sourceRow)
            val cellDefinitions = computeCells(rowDefinitions)
            val rowCellAttributes = computeRowLevelCellAttributes(rowDefinitions)
            val cellValues = tableModel.columns.mapIndexed { index: Int, column: Column<T> ->
                cellDefinitions.resolveCell(column, sourceRow)?.let { value ->
                    stateAndAttributes.createCellContext(
                        relativeRowIndex = tableRowIndex,
                        relativeColumnIndex = column.index ?: index,
                        value = value,
                        attributes = mergeLatterWins(
                            tableModel.cellAttributes,
                            column.cellAttributes,
                            rowCellAttributes,
                            cellDefinitions[column.id]?.cellAttributes
                        )
                    ).let { Pair(column.id, it) }
                }
            }.mapNotNull { it }.toMap()
            stateAndAttributes.createRowContext(
                relativeRowIndex = tableRowIndex,
                rowAttributes = computeRowAttributes(rowDefinitions),
                cells = cellValues
            )
        }
    }

    private fun resolveRowContext(
        tableRowIndex: Int,
        indexedRecord: IndexedValue<T>? = null
    ): IndexedValue<AttributedRow<T>> {
        return IndexedValue(tableRowIndex, resolveAttributedRow(tableRowIndex, indexedRecord))
    }

    override fun resolve(requestedIndex: Int): IndexedValue<AttributedRow<T>>? {
        return if (tableModel.hasRowsAt(requestedIndex)) {
            resolveRowContext(requestedIndex)
        } else {
            getNextRecord().let {
                if (it != null) {
                    resolveRowContext(requestedIndex, it)
                } else {
                    tableModel.getNextCustomRowIndex(requestedIndex)?.let { nextTableRowIndex ->
                        resolveRowContext(nextTableRowIndex)
                    }
                }
            }
        }
    }

    protected abstract fun getNextRecord(): IndexedValue<T>?
}
