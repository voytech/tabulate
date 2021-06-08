package io.github.voytech.tabulate.template.resolvers

import io.github.voytech.tabulate.model.*
import io.github.voytech.tabulate.model.attributes.alias.CellAttribute
import io.github.voytech.tabulate.model.attributes.alias.RowAttribute
import io.github.voytech.tabulate.model.attributes.overrideAttributesLeftToRight
import io.github.voytech.tabulate.template.context.AttributedRow
import io.github.voytech.tabulate.template.context.ExportingStateReceiver
import io.github.voytech.tabulate.template.context.RowIndex
import io.github.voytech.tabulate.template.context.TableExportingState

abstract class AbstractRowContextResolver<T> :
    IndexedContextResolver<T, AttributedRow<T>>, ExportingStateReceiver<T> {

    private lateinit var tableExportingState: TableExportingState<T>
    private lateinit var tableModel: Table<T>

    private fun computeCells(rowDefinitions: Set<RowDef<T>>): Map<ColumnKey<T>, CellDef<T>> {
        return rowDefinitions.mapNotNull { row -> row.cells }.fold(mapOf(), { acc, m -> acc + m })
    }

    private fun computeRowLevelCellAttributes(rowDefinitions: Set<RowDef<T>>): Set<CellAttribute> {
        return overrideAttributesLeftToRight(*(rowDefinitions.mapNotNull { i -> i.cellAttributes }.toTypedArray()))
    }

    private fun computeRowAttributes(rowDefinitions: Set<RowDef<T>>): Set<RowAttribute> {
        return rowDefinitions.mapNotNull { attribs -> attribs.rowAttributes }
            .fold(setOf(), { acc, r -> acc + r })
    }

    private fun resolveAttributedRow(tableRowIndex: RowIndex, record: IndexedValue<T>? = null): AttributedRow<T> {
        return SourceRow(
            rowIndex = tableRowIndex,
            objectIndex = record?.index,
            record = record?.value
        ).let { sourceRow ->
            val rowDefinitions = tableModel.getRows(sourceRow)
            val cellDefinitions = computeCells(rowDefinitions)
            val rowCellAttributes = computeRowLevelCellAttributes(rowDefinitions)
            val cellValues = tableModel.columns.mapIndexed { index: Int, column: ColumnDef<T> ->
                cellDefinitions.resolveCellValue(column, sourceRow)?.let { value ->
                    tableExportingState.createCellContext(
                        relativeRowIndex = tableRowIndex.rowIndex,
                        relativeColumnIndex = column.index ?: index,
                        value = value,
                        attributes = overrideAttributesLeftToRight(
                            tableModel.cellAttributes,
                            column.cellAttributes,
                            rowCellAttributes,
                            cellDefinitions[column.id]?.cellAttributes
                        )
                    ).let { Pair(column.id, it) }
                }
            }.mapNotNull { it }.toMap()
            tableExportingState.createRowContext(
                relativeRowIndex = tableRowIndex.rowIndex,
                rowAttributes = computeRowAttributes(rowDefinitions),
                cells = cellValues
            )
        }
    }

    private fun resolveRowContext(
        tableRowIndex: RowIndex,
        indexedRecord: IndexedValue<T>? = null,
    ): IndexedValue<AttributedRow<T>> {
        return IndexedValue(tableRowIndex.rowIndex, resolveAttributedRow(tableRowIndex, indexedRecord))
    }

    override fun resolve(requestedIndex: RowIndex): IndexedValue<AttributedRow<T>>? {
        return if (tableModel.hasCustomRows(SourceRow(requestedIndex))) {
            resolveRowContext(requestedIndex)
        } else {
            getNextRecord().let {
                if (it != null) {
                    resolveRowContext(requestedIndex, it)
                } else {
                    tableModel.getNextCustomRowIndex(requestedIndex)
                        ?.let { nextIndexDef ->
                            resolveRowContext(requestedIndex + nextIndexDef)
                        }
                }
            }
        }
    }

    override fun setState(exportingState: TableExportingState<T>) {
        tableExportingState = exportingState
        tableModel = exportingState.tableModel
    }

    protected abstract fun getNextRecord(): IndexedValue<T>?
}
