package pl.voytech.exporter.core.template.resolvers

import pl.voytech.exporter.core.model.*
import pl.voytech.exporter.core.model.attributes.mergeAttributes
import pl.voytech.exporter.core.template.context.*

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

    private fun resolveAttributedRow(resolvedIndex: Int, record: IndexedValue<T>? = null): AttributedRow<T> {
        return SourceRow(rowIndex = resolvedIndex, objectIndex = record?.index, record = record?.value).let {
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
                            relativeRowIndex = resolvedIndex,
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
                relativeRowIndex = resolvedIndex,
                rowAttributes = rowDefinitions.mapNotNull { attribs -> attribs.rowAttributes }
                    .fold(setOf(), { acc, r -> acc + r }),
                cells = cellValues.toMap()
            )
        }
    }

    private fun resolveRowContext(
        resolvedIndex: Int,
        indexedRecord: IndexedValue<T>? = null
    ): IndexedValue<AttributedRow<T>> {
        return IndexedValue(resolvedIndex, resolveAttributedRow(resolvedIndex, indexedRecord))
    }

    override fun resolve(requestedIndex: Int): IndexedValue<AttributedRow<T>>? {
        return if (tableModel.hasRowsAt(requestedIndex)) {
            resolveRowContext(requestedIndex)
        } else {
            getNextRecord().let {
                if (it != null) {
                    resolveRowContext(requestedIndex, it)
                } else {
                    tableModel.getNextCustomRowIndex(requestedIndex)?.let { it1 -> resolveRowContext(it1) }
                }
            }
        }
    }

    protected abstract fun getNextRecord(): IndexedValue<T>?
}
