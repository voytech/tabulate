package pl.voytech.exporter.core.template.operations

import pl.voytech.exporter.core.model.*
import pl.voytech.exporter.core.model.attributes.CellAttribute
import pl.voytech.exporter.core.model.attributes.ColumnAttribute
import pl.voytech.exporter.core.model.attributes.RowAttribute
import pl.voytech.exporter.core.model.attributes.TableAttribute
import pl.voytech.exporter.core.template.*
import java.util.*

abstract class AttributesHandlingTableOperations<T, A>(
    tableAttributeOperations: List<TableAttributeOperation<out TableAttribute, A>>?,
    columnAttributeOperations: List<ColumnAttributeOperation<T, out ColumnAttribute, A>>?,
    rowAttributeOperations: List<RowAttributeOperation<T, out RowAttribute, A>>?,
    cellAttributeOperations: List<CellAttributeOperation<T, out CellAttribute, A>>?
) : TableOperations<T, A> {

    private val tableAttributeOperationsByClass: Map<Class<out TableAttribute>, TableAttributeOperation<TableAttribute, A>> =
        tableAttributeOperations?.groupBy { it.attributeType() }
            ?.map { it.key to it.value.first() as TableAttributeOperation<TableAttribute, A> }
            ?.sortedBy { it.second.priority() }
            ?.toMap() ?: emptyMap()

    private val columnAttributeOperationsByClass: Map<Class<out ColumnAttribute>, ColumnAttributeOperation<T, ColumnAttribute, A>> =
        columnAttributeOperations?.groupBy { it.attributeType() }
            ?.map { it.key to it.value.first() as ColumnAttributeOperation<T, ColumnAttribute, A> }
            ?.sortedBy { it.second.priority() }
            ?.toMap() ?: emptyMap()

    private val rowAttributeOperationsByClass: Map<Class<out RowAttribute>, RowAttributeOperation<T, RowAttribute, A>> =
        rowAttributeOperations?.groupBy { it.attributeType() }
            ?.map { it.key to it.value.first() as RowAttributeOperation<T, RowAttribute, A> }
            ?.sortedBy { it.second.priority() }
            ?.toMap() ?: emptyMap()

    private val cellAttributeOperationsByClass: Map<Class<out CellAttribute>, CellAttributeOperation<T, CellAttribute, A>> =
        cellAttributeOperations?.groupBy { it.attributeType() }
            ?.map { it.key to it.value.first() as CellAttributeOperation<T, CellAttribute, A> }
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
        return cells?.map { it.key to Cell(
            value = it.value.value,
            eval = it.value.eval,
            type = it.value.type,
            colSpan = it.value.colSpan,
            rowSpan = it.value.rowSpan,
            cellAttributes = sortedCellAttributes(it.value.cellAttributes)
        ) }?.toMap()
    }

    private fun sortedAttributeOperationsTable(table: Table<T>): Table<T> {
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
    override fun createTable(state: DelegateAPI<A>, table: Table<T>): Table<T> {
        return sortedAttributeOperationsTable(table).let { sortedTable ->
            sortedTable.tableAttributes?.forEach { tableAttribute ->
                tableAttributeOperationsByClass[tableAttribute.javaClass]?.renderAttribute(
                    state,
                    table,
                    tableAttribute
                )
            }
            initializeTable(state, sortedTable)
        }
    }

    abstract fun initializeTable(state: DelegateAPI<A>, table: Table<T>): Table<T>

    @Suppress("TYPE_INFERENCE_ONLY_INPUT_TYPES_WARNING")
    override fun renderRow(
        state: DelegateAPI<A>,
        context: OperationContext<T, RowOperationTableData<T>>
    ) {
        context.data.rowAttributes?.let { attributes ->
            var operationRendered = false
            attributes.forEach { attribute ->
                rowAttributeOperationsByClass[attribute.javaClass]?.let { operation ->
                    if (operation.priority() >= 0 && !operationRendered) {
                        renderRowValue(state, context)
                        operationRendered = true
                    }
                    operation.renderAttribute(state, context, attribute)
                }
            }
        } ?: renderRowValue(state, context)
    }

    abstract fun renderRowValue(state: DelegateAPI<A>, context: OperationContext<T, RowOperationTableData<T>>)

    @Suppress("TYPE_INFERENCE_ONLY_INPUT_TYPES_WARNING")
    override fun renderColumn(
        state: DelegateAPI<A>,
        context: OperationContext<T, ColumnOperationTableData<T>>
    ) {
        context.data.columnAttributes?.let { attributes ->
            attributes.forEach { attribute ->
                columnAttributeOperationsByClass[attribute.javaClass]?.renderAttribute(state, context, attribute)
            }
        }
    }

    @Suppress("TYPE_INFERENCE_ONLY_INPUT_TYPES_WARNING")
    override fun renderRowCell(
        state: DelegateAPI<A>,
        context: OperationContext<T, CellOperationTableData<T>>
    ) {
        if (!context.data.cellValue?.attributes.isNullOrEmpty()) {
            var operationRendered = false
            context.data.cellValue?.attributes?.forEach { attribute ->
                cellAttributeOperationsByClass[attribute.javaClass]?.let { operation ->
                    if (operation.priority() >= 0 && !operationRendered) {
                        renderRowCellValue(state, context)
                        operationRendered = true
                    }
                    operation.renderAttribute(state, context, attribute)
                }
            }
        } else {
            renderRowCellValue(state, context)
        }
    }

    abstract fun renderRowCellValue(state: DelegateAPI<A>, context: OperationContext<T, CellOperationTableData<T>>)

}

