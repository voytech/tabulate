package pl.voytech.exporter.core.template.operations.impl

import pl.voytech.exporter.core.model.*
import pl.voytech.exporter.core.model.attributes.CellAttribute
import pl.voytech.exporter.core.model.attributes.ColumnAttribute
import pl.voytech.exporter.core.model.attributes.RowAttribute
import pl.voytech.exporter.core.model.attributes.TableAttribute
import pl.voytech.exporter.core.template.context.*
import pl.voytech.exporter.core.template.operations.*
import java.util.*

class AttributeAwareTableOperations<T>(
    tableAttributeOperations: Set<TableAttributeOperation<out TableAttribute>>?,
    columnAttributeOperations: Set<ColumnAttributeOperation<T, out ColumnAttribute>>?,
    rowAttributeOperations: Set<RowAttributeOperation<T, out RowAttribute>>?,
    cellAttributeOperations: Set<CellAttributeOperation<T, out CellAttribute>>?,
    private val baseTableOperations: TableOperations<T>
) : TableOperations<T> {

    private val tableAttributeOperationsByClass: Map<Class<out TableAttribute>, TableAttributeOperation<TableAttribute>> =
        tableAttributeOperations?.groupBy { it.attributeType() }
            ?.map { it.key to it.value.first() as TableAttributeOperation<TableAttribute> }
            ?.sortedBy { it.second.priority() }
            ?.toMap() ?: emptyMap()

    private val columnAttributeOperationsByClass: Map<Class<out ColumnAttribute>, ColumnAttributeOperation<T, ColumnAttribute>> =
        columnAttributeOperations?.groupBy { it.attributeType() }
            ?.map { it.key to it.value.first() as ColumnAttributeOperation<T, ColumnAttribute> }
            ?.sortedBy { it.second.priority() }
            ?.toMap() ?: emptyMap()

    private val rowAttributeOperationsByClass: Map<Class<out RowAttribute>, RowAttributeOperation<T, RowAttribute>> =
        rowAttributeOperations?.groupBy { it.attributeType() }
            ?.map { it.key to it.value.first() as RowAttributeOperation<T, RowAttribute> }
            ?.sortedBy { it.second.priority() }
            ?.toMap() ?: emptyMap()

    private val cellAttributeOperationsByClass: Map<Class<out CellAttribute>, CellAttributeOperation<T, CellAttribute>> =
        cellAttributeOperations?.groupBy { it.attributeType() }
            ?.map { it.key to it.value.first() as CellAttributeOperation<T, CellAttribute> }
            ?.sortedBy { it.second.priority() }
            ?.toMap() ?: emptyMap()

    private fun sortedTableAttributes(tableAttributes: Set<TableAttribute>?): SortedSet<TableAttribute>? {
        return tableAttributes?.toSortedSet(compareBy {
            tableAttributeOperationsByClass[it.javaClass]?.priority() ?: 0
        })
    }

    private fun sortedColumnAttributes(columnAttributes: Set<ColumnAttribute>?): SortedSet<ColumnAttribute>? {
        return columnAttributes?.toSortedSet(compareBy {
            columnAttributeOperationsByClass[it.javaClass]?.priority() ?: 0
        })
    }

    private fun sortedCellAttributes(cellAttributes: Set<CellAttribute>?): SortedSet<CellAttribute>? {
        return cellAttributes?.toSortedSet(compareBy { cellAttributeOperationsByClass[it.javaClass]?.priority() ?: 0 })
    }

    private fun sortedRowAttributes(rowAttributes: Set<RowAttribute>?): SortedSet<RowAttribute>? {
        return rowAttributes?.toSortedSet(compareBy { rowAttributeOperationsByClass[it.javaClass]?.priority() ?: 0 })
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
        return baseTableOperations.createTable(sortByAttributeOperationPriority(table)).also { sortedTable ->
            sortedTable.tableAttributes?.forEach { tableAttribute ->
                tableAttributeOperationsByClass[tableAttribute.javaClass]?.renderAttribute(table, tableAttribute)
            }
        }
    }

    @Suppress("TYPE_INFERENCE_ONLY_INPUT_TYPES_WARNING")
    override fun renderRow(context: AttributedRow<T>) {
        if (!context.rowAttributes.isNullOrEmpty()) {
            var operationRendered = false
            context.rowAttributes.forEach { attribute ->
                rowAttributeOperationsByClass[attribute.javaClass]?.let { operation ->
                    if (operation.priority() >= 0 && !operationRendered) {
                        baseTableOperations.renderRow(context)
                        operationRendered = true
                    }
                    operation.renderAttribute(context, attribute)
                }
            }
        } else {
            baseTableOperations.renderRow(context)
        }
    }

    @Suppress("TYPE_INFERENCE_ONLY_INPUT_TYPES_WARNING")
    override fun renderColumn(context: AttributedColumn) {
        context.columnAttributes?.let { attributes ->
            attributes.forEach { attribute ->
                columnAttributeOperationsByClass[attribute.javaClass]?.renderAttribute(context, attribute)
            }
        }
    }

    @Suppress("TYPE_INFERENCE_ONLY_INPUT_TYPES_WARNING")
    override fun renderRowCell(context: AttributedCell) {
        if (!context.attributes.isNullOrEmpty()) {
            var operationRendered = false
            context.attributes.forEach { attribute ->
                cellAttributeOperationsByClass[attribute.javaClass]?.let { operation ->
                    if (operation.priority() >= 0 && !operationRendered) {
                        baseTableOperations.renderRowCell(context)
                        operationRendered = true
                    }
                    operation.renderAttribute(context, attribute)
                }
            }
        } else {
            baseTableOperations.renderRowCell(context)
        }
    }

}

