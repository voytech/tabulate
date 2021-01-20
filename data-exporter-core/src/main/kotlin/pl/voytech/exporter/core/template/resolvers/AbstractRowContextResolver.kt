package pl.voytech.exporter.core.template.resolvers

import pl.voytech.exporter.core.model.*
import pl.voytech.exporter.core.model.attributes.mergeAttributes
import pl.voytech.exporter.core.template.AttributedCell
import pl.voytech.exporter.core.template.AttributedRow
import pl.voytech.exporter.core.template.CellValue
import pl.voytech.exporter.core.template.OperationContext

abstract class AbstractRowContextResolver<DS,T>(tableModel: Table<T>) :
    TableDataSourceIndexedContextResolver<DS, T, AttributedRow<T>>(
        tableModel
    ) {

    private fun resolveAttributedRow(resolvedIndex: Int, record: IndexedValue<T>? = null): AttributedRow<T> {
        return SourceRow(
            dataset = if (dataSource is Collection<*>) dataSource as Collection<T> else emptyList(),
            rowIndex = resolvedIndex,
            objectIndex = record?.index,
            record = record?.value
        ).let {
            val rowDefinitions: Set<Row<T>> = tableModel.getRowsFor(it)
            val cellDefinitions: Map<ColumnKey<T>, Cell<T>> =
                rowDefinitions.mapNotNull { row -> row.cells }.fold(mapOf(), { acc, m -> acc + m })
            val cellValues: MutableMap<ColumnKey<T>, AttributedCell> = mutableMapOf()
            val rowCellExtensions = mergeAttributes(
                *(rowDefinitions.mapNotNull { i -> i.cellAttributes }.toTypedArray())
            )
            tableModel.forEachColumn { column: Column<T> ->
                if (/*state.dontSkip(column)*/ true) {
                    val cellDefinition = cellDefinitions?.get(column.id)
                    // state.applySpans(column, cellDefinition)
                    val attributedCell = computeCellValue(column, cellDefinition, it)?.let {
                        AttributedCell(
                            value = CellValue(
                                it,
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
            AttributedRow(
                rowAttributes = rowDefinitions.mapNotNull { attribs ->  attribs.rowAttributes }.fold(setOf(), { acc, r -> acc + r }),
                rowCellValues = cellValues.toMap()
            )
        }
    }

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

    override fun resolve(requestedIndex: Int): IndexedValue<OperationContext<AttributedRow<T>>>? {
        if (tableModel.hasRowsAt(requestedIndex)) {
            return resolveAttributedRow(requestedIndex).let {
                val ctx = OperationContext<AttributedRow<T>>(mutableMapOf())
                IndexedValue(requestedIndex, ctx)
            }
        }
        return null
    }

    protected abstract fun getNextDataSourceRecord(): T
}
