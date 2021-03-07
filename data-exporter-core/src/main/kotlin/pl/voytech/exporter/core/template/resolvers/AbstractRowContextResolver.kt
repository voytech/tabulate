package pl.voytech.exporter.core.template.resolvers

import pl.voytech.exporter.core.model.*
import pl.voytech.exporter.core.model.attributes.mergeAttributes
import pl.voytech.exporter.core.template.context.AttributedCell
import pl.voytech.exporter.core.template.context.AttributedRow
import pl.voytech.exporter.core.template.context.CellValue
import pl.voytech.exporter.core.template.context.GlobalContextAndAttributes

abstract class AbstractRowContextResolver<DS, T>(tableModel: Table<T>, stateAndAttributes: GlobalContextAndAttributes<T>) :
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

    private fun resolveAttributedRow(tableRowIndex: Int, record: IndexedValue<T>? = null): AttributedRow<T> {
        return SourceRow(rowIndex = tableRowIndex, objectIndex = record?.index, record = record?.value).let {
            val rowDefinitions: Set<Row<T>> = tableModel.getRowsFor(it)
            val cellDefinitions: Map<ColumnKey<T>, Cell<T>> =
                rowDefinitions.mapNotNull { row -> row.cells }.fold(mapOf(), { acc, m -> acc + m })
            val cellValues: MutableMap<ColumnKey<T>, AttributedCell> = mutableMapOf()
            val rowCellExtensions = mergeAttributes(
                *(rowDefinitions.mapNotNull { i -> i.cellAttributes }.toTypedArray())
            )
            tableModel.forEachColumn { index: Int, column: Column<T> ->
                if (stateAndAttributes.dontSkip(column)) {
                    val cellDefinition = cellDefinitions[column.id]
                    stateAndAttributes.applySpans(column, cellDefinition)
                    val attributedCell = computeCellValue(column, cellDefinition, it)?.let {  value ->
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
                                rowCellExtensions,
                                cellDefinition?.cellAttributes
                            )
                        )
                    }
                    if (attributedCell != null) {
                        cellValues[column.id] = attributedCell
                    }
                }
            }
            stateAndAttributes.createRowContext(
                relativeRowIndex = tableRowIndex,
                rowAttributes = rowDefinitions.mapNotNull { attribs -> attribs.rowAttributes }
                    .fold(setOf(), { acc, r -> acc + r }),
                cells = cellValues.toMap()
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
