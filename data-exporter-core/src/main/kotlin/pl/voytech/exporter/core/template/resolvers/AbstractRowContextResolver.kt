package pl.voytech.exporter.core.template.resolvers

import pl.voytech.exporter.core.model.*
import pl.voytech.exporter.core.model.attributes.CellAttribute
import pl.voytech.exporter.core.model.attributes.RowAttribute
import pl.voytech.exporter.core.model.attributes.mergeAttributes
import pl.voytech.exporter.core.template.context.AttributedCell
import pl.voytech.exporter.core.template.context.AttributedRow
import pl.voytech.exporter.core.template.context.CellValue
import pl.voytech.exporter.core.template.context.GlobalContextAndAttributes

abstract class AbstractRowContextResolver<DS, T>(
    tableModel: Table<T>,
    stateAndAttributes: GlobalContextAndAttributes<T>
) :
    TableDataSourceContextResolver<DS, T>(tableModel, stateAndAttributes) {

    private inline fun computeCellValue(
        column: Column<T>,
        customCell: Cell<T>?,
        sourceRow: SourceRow<T>
    ): Any? {
        return (customCell?.eval?.invoke(sourceRow) ?: customCell?.value ?: sourceRow.record?.let {
            column.id.ref?.invoke(it)
        })?.let {
            column.dataFormatter?.invoke(it) ?: it
        }
    }

    private fun computeCells(rowDefinitions: Set<Row<T>>): Map<ColumnKey<T>, Cell<T>> {
        return rowDefinitions.mapNotNull { row -> row.cells }.fold(mapOf(), { acc, m -> acc + m })
    }

    private fun computeRowLevelCellAttributes(rowDefinitions: Set<Row<T>>): Set<CellAttribute<*>> {
        return mergeAttributes(*(rowDefinitions.mapNotNull { i -> i.cellAttributes }.toTypedArray()))
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
                    if (stateAndAttributes.dontSkip(column)) {
                        val cellDefinition = cellDefinitions[column.id]
                        stateAndAttributes.applySpans(column, cellDefinition)
                        computeCellValue(column, cellDefinition, sourceRow)!!.let { value ->
                            stateAndAttributes.createCellContext(
                                relativeRowIndex = tableRowIndex,
                                relativeColumnIndex = column.index ?: index,
                                value = CellValue(
                                    value,
                                    cellDefinition?.type ?: column.columnType,
                                    colSpan = cellDefinition?.colSpan ?: 1,
                                    rowSpan = cellDefinition?.rowSpan ?: 1
                                ),
                                attributes = mergeAttributes(
                                    tableModel.cellAttributes,
                                    column.cellAttributes,
                                    rowCellAttributes,
                                    cellDefinition?.cellAttributes
                                )
                            ).let { Pair(column.id, it) }
                        }
                    } else null
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
