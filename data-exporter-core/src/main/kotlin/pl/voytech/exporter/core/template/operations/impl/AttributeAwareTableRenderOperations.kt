package pl.voytech.exporter.core.template.operations.impl

import pl.voytech.exporter.core.model.*
import pl.voytech.exporter.core.model.attributes.CellAttribute
import pl.voytech.exporter.core.model.attributes.ColumnAttribute
import pl.voytech.exporter.core.model.attributes.RowAttribute
import pl.voytech.exporter.core.model.attributes.TableAttribute
import pl.voytech.exporter.core.template.context.*
import pl.voytech.exporter.core.template.operations.*
import java.util.*

class AttributeAwareTableRenderOperations<T>(
    tableAttributeRenderOperations: Set<TableAttributeRenderOperation<out TableAttribute>>?,
    columnAttributeRenderOperations: Set<ColumnAttributeRenderOperation<T, out ColumnAttribute>>?,
    rowAttributeRenderOperations: Set<RowAttributeRenderOperation<T, out RowAttribute>>?,
    cellAttributeRenderOperations: Set<CellAttributeRenderOperation<T, out CellAttribute>>?,
    private val baseTableRenderOperations: TableRenderOperations<T>
) : TableRenderOperations<T> {

    private val tableAttributeRenderOperationsByClass: Map<Class<out TableAttribute>, TableAttributeRenderOperation<TableAttribute>> =
        tableAttributeRenderOperations?.groupBy { it.attributeType() }
            ?.map { it.key to it.value.first() as TableAttributeRenderOperation<TableAttribute> }
            ?.sortedBy { it.second.priority() }
            ?.toMap() ?: emptyMap()

    private val columnAttributeRenderOperationsByClass: Map<Class<out ColumnAttribute>, ColumnAttributeRenderOperation<T, ColumnAttribute>> =
        columnAttributeRenderOperations?.groupBy { it.attributeType() }
            ?.map { it.key to it.value.first() as ColumnAttributeRenderOperation<T, ColumnAttribute> }
            ?.sortedBy { it.second.priority() }
            ?.toMap() ?: emptyMap()

    private val rowAttributeRenderOperationsByClass: Map<Class<out RowAttribute>, RowAttributeRenderOperation<T, RowAttribute>> =
        rowAttributeRenderOperations?.groupBy { it.attributeType() }
            ?.map { it.key to it.value.first() as RowAttributeRenderOperation<T, RowAttribute> }
            ?.sortedBy { it.second.priority() }
            ?.toMap() ?: emptyMap()

    private val cellAttributeRenderOperationsByClass: Map<Class<out CellAttribute>, CellAttributeRenderOperation<T, CellAttribute>> =
        cellAttributeRenderOperations?.groupBy { it.attributeType() }
            ?.map { it.key to it.value.first() as CellAttributeRenderOperation<T, CellAttribute> }
            ?.sortedBy { it.second.priority() }
            ?.toMap() ?: emptyMap()

    private fun sortedTableAttributes(tableAttributes: Set<TableAttribute>?): SortedSet<TableAttribute>? {
        return tableAttributes?.toSortedSet(compareBy {
            tableAttributeRenderOperationsByClass[it.javaClass]?.priority() ?: 0
        })
    }

    private fun sortedColumnAttributes(columnAttributes: Set<ColumnAttribute>?): SortedSet<ColumnAttribute>? {
        return columnAttributes?.toSortedSet(compareBy {
            columnAttributeRenderOperationsByClass[it.javaClass]?.priority() ?: 0
        })
    }

    private fun sortedCellAttributes(cellAttributes: Set<CellAttribute>?): SortedSet<CellAttribute>? {
        return cellAttributes?.toSortedSet(compareBy { cellAttributeRenderOperationsByClass[it.javaClass]?.priority() ?: 0 })
    }

    private fun sortedRowAttributes(rowAttributes: Set<RowAttribute>?): SortedSet<RowAttribute>? {
        return rowAttributes?.toSortedSet(compareBy { rowAttributeRenderOperationsByClass[it.javaClass]?.priority() ?: 0 })
    }

    private fun columnsWithSortedAttributes(columns: List<Column<T>>): List<Column<T>> {
        return columns.map {
            Column(
                id = it.id,
                index = it.index,
                columnType = it.columnType,
                columnAttributes = sortedColumnAttributes(it.columnAttributes),
                cellAttributes = sortedCellAttributes(it.cellAttributes),
                dataFormatter = it.dataFormatter
            )
        }
    }

    private fun rowsWithSortedAttributes(rows: List<Row<T>>?): List<Row<T>>? {
        return rows?.map {
            Row(
                selector = it.selector,
                createAt = it.createAt,
                cellAttributes = sortedCellAttributes(it.cellAttributes),
                rowAttributes = sortedRowAttributes(it.rowAttributes),
                cells = cellsWithSortedAttributes(it.cells)
            )
        }
    }

    private fun cellsWithSortedAttributes(cells: Map<ColumnKey<T>, Cell<T>>?): Map<ColumnKey<T>, Cell<T>>? {
        return cells?.map {
            it.key to Cell(
                value = it.value.value,
                eval = it.value.eval,
                type = it.value.type,
                colSpan = it.value.colSpan,
                rowSpan = it.value.rowSpan,
                cellAttributes = sortedCellAttributes(it.value.cellAttributes)
            )
        }?.toMap()
    }

    private fun sortByAttributeOperationPriority(table: Table<T>): Table<T> {
        return Table(
            name = table.name,
            firstRow = table.firstRow,
            firstColumn = table.firstColumn,
            columns = columnsWithSortedAttributes(table.columns),
            rows = rowsWithSortedAttributes(table.rows),
            tableAttributes = sortedTableAttributes(table.tableAttributes),
            cellAttributes = sortedCellAttributes(table.cellAttributes)
        )
    }

    @Suppress("TYPE_INFERENCE_ONLY_INPUT_TYPES_WARNING")
    override fun createTable(table: Table<T>): Table<T> {
        return baseTableRenderOperations.createTable(sortByAttributeOperationPriority(table)).also { sortedTable ->
            sortedTable.tableAttributes?.forEach { tableAttribute ->
                tableAttributeRenderOperationsByClass[tableAttribute.javaClass]?.renderAttribute(table, tableAttribute)
            }
        }
    }

    @Suppress("TYPE_INFERENCE_ONLY_INPUT_TYPES_WARNING")
    override fun renderRow(context: AttributedRow<T>) {
        if (!context.rowAttributes.isNullOrEmpty()) {
            var operationRendered = false
            context.rowAttributes.forEach { attribute ->
                rowAttributeRenderOperationsByClass[attribute.javaClass]?.let { operation ->
                    if (operation.priority() >= 0 && !operationRendered) {
                        baseTableRenderOperations.renderRow(context)
                        operationRendered = true
                    }
                    operation.renderAttribute(context, attribute)
                }
            }
        } else {
            baseTableRenderOperations.renderRow(context)
        }
    }

    @Suppress("TYPE_INFERENCE_ONLY_INPUT_TYPES_WARNING")
    override fun renderColumn(context: AttributedColumn) {
        context.columnAttributes?.let { attributes ->
            attributes.forEach { attribute ->
                columnAttributeRenderOperationsByClass[attribute.javaClass]?.renderAttribute(context, attribute)
            }
        }
    }

    @Suppress("TYPE_INFERENCE_ONLY_INPUT_TYPES_WARNING")
    override fun renderRowCell(context: AttributedCell) {
        if (!context.attributes.isNullOrEmpty()) {
            var operationRendered = false
            context.attributes.forEach { attribute ->
                cellAttributeRenderOperationsByClass[attribute.javaClass]?.let { operation ->
                    if (operation.priority() >= 0 && !operationRendered) {
                        baseTableRenderOperations.renderRowCell(context)
                        operationRendered = true
                    }
                    operation.renderAttribute(context, attribute)
                }
            }
        } else {
            baseTableRenderOperations.renderRowCell(context)
        }
    }

}

