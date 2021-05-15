package io.github.voytech.tabulate.core.template.resolvers

import io.github.voytech.tabulate.core.model.*
import io.github.voytech.tabulate.core.model.attributes.alias.CellAttribute
import io.github.voytech.tabulate.core.model.attributes.alias.RowAttribute
import io.github.voytech.tabulate.core.model.attributes.overrideAttributesRightToLeft
import io.github.voytech.tabulate.core.template.context.AttributedRow
import io.github.voytech.tabulate.core.template.context.GlobalContextAndAttributes

abstract class AbstractRowContextResolver<T>(
    tableModel: Table<T>,
    stateAndAttributes: GlobalContextAndAttributes<T>
) :
    GlobalStateAwareContextResolver<T>(tableModel, stateAndAttributes) {

    private fun computeCells(rowDefinitions: Set<Row<T>>): Map<ColumnKey<T>, Cell<T>> {
        return rowDefinitions.mapNotNull { row -> row.cells }.fold(mapOf(), { acc, m -> acc + m })
    }

    private fun computeRowLevelCellAttributes(rowDefinitions: Set<Row<T>>): Set<CellAttribute> {
        return overrideAttributesRightToLeft(*(rowDefinitions.mapNotNull { i -> i.cellAttributes }.toTypedArray()))
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
                cellDefinitions.resolveCellValue(column, sourceRow)?.let { value ->
                    stateAndAttributes.createCellContext(
                        relativeRowIndex = tableRowIndex,
                        relativeColumnIndex = column.index ?: index,
                        value = value,
                        attributes = overrideAttributesRightToLeft(
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
