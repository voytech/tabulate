package pl.voytech.exporter.core.template.operations.impl

import pl.voytech.exporter.core.api.builder.TableBuilder
import pl.voytech.exporter.core.model.Table
import pl.voytech.exporter.core.model.attributes.ColumnAttribute
import pl.voytech.exporter.core.model.attributes.RowAttribute
import pl.voytech.exporter.core.model.attributes.TableAttribute
import pl.voytech.exporter.core.model.attributes.alias.CellAttribute
import pl.voytech.exporter.core.template.context.AttributedCell
import pl.voytech.exporter.core.template.context.AttributedColumn
import pl.voytech.exporter.core.template.context.AttributedRow
import pl.voytech.exporter.core.template.operations.*
import pl.voytech.exporter.core.model.attributes.CellAttribute as CellAttributeClass

@Suppress("UNCHECKED_CAST")
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
            ?.toMap() ?: emptyMap()

    private val columnAttributeRenderOperationsByClass: Map<Class<out ColumnAttribute>, ColumnAttributeRenderOperation<T, ColumnAttribute>> =
        columnAttributeRenderOperations?.groupBy { it.attributeType() }
            ?.map { it.key to it.value.first() as ColumnAttributeRenderOperation<T, ColumnAttribute> }
            ?.toMap() ?: emptyMap()

    private val rowAttributeRenderOperationsByClass: Map<Class<out RowAttribute>, RowAttributeRenderOperation<T, RowAttribute>> =
        rowAttributeRenderOperations?.groupBy { it.attributeType() }
            ?.map { it.key to it.value.first() as RowAttributeRenderOperation<T, RowAttribute> }
            ?.toMap() ?: emptyMap()

    private val cellAttributeRenderOperationsByClass: Map<Class<out CellAttribute>, CellAttributeRenderOperation<T, CellAttribute>> =
        cellAttributeRenderOperations?.groupBy { it.attributeType() }
            ?.map { it.key to it.value.first() as CellAttributeRenderOperation<T, CellAttribute> }
            ?.sortedBy { it.second.priority() }
            ?.toMap() ?: emptyMap()

    private fun sortedTableAttributes(tableAttributes: Set<TableAttribute>?): Set<TableAttribute>? {
        return tableAttributes?.toSortedSet(compareBy {
            tableAttributeRenderOperationsByClass[it.javaClass]?.priority() ?: 0
        })
    }

    private fun sortedColumnAttributes(columnAttributes: Set<ColumnAttribute>?): Set<ColumnAttribute>? {
        return columnAttributes?.toSortedSet(compareBy {
            columnAttributeRenderOperationsByClass[it.javaClass]?.priority() ?: 0
        })
    }

    private fun sortedCellAttributes(cellAttributes: Set<CellAttribute>?): Set<CellAttribute>? {
        return cellAttributes?.sortedBy { cellAttributeRenderOperationsByClass[it.javaClass]?.priority() ?: 0 }?.toSet()
    }

    private fun sortedRowAttributes(rowAttributes: Set<RowAttribute>?): Set<RowAttribute>? {
        return rowAttributes?.toSortedSet(compareBy { rowAttributeRenderOperationsByClass[it.javaClass]?.priority() ?: 0 })
    }

    private fun withAllAttributesSorted(builder: TableBuilder<T>): TableBuilder<T> {
        builder.columnsBuilder.columnBuilders.forEach { columnBuilder ->
            columnBuilder.visit(CellAttributeClass::class.java) { sortedCellAttributes(it) }
            columnBuilder.visit(ColumnAttribute::class.java) { sortedColumnAttributes(it) }
        }
        builder.rowsBuilder.rowBuilders.forEach { rowBuilder ->
            rowBuilder.visit(CellAttributeClass::class.java) { sortedCellAttributes(it) }
            rowBuilder.visit(RowAttribute::class.java) { sortedRowAttributes(it) }
            rowBuilder.cellsBuilder.cells.forEach { (_, cellBuilder) ->
                cellBuilder.visit(CellAttributeClass::class.java) { sortedCellAttributes(it) }
            }
        }
        builder.visit(TableAttribute::class.java) { sortedTableAttributes(it) }
        builder.visit(CellAttributeClass::class.java) { sortedCellAttributes(it) }
        return builder
    }

    override fun createTable(builder: TableBuilder<T>): Table<T> {
        return baseTableRenderOperations.createTable(withAllAttributesSorted(builder)).also { sortedTable ->
            sortedTable.tableAttributes?.forEach { tableAttribute ->
                tableAttributeRenderOperationsByClass[tableAttribute.javaClass]?.renderAttribute(sortedTable, tableAttribute)
            }
        }
    }

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

    override fun renderColumn(context: AttributedColumn) {
        context.columnAttributes?.let { attributes ->
            attributes.forEach { attribute ->
                columnAttributeRenderOperationsByClass[attribute.javaClass]?.renderAttribute(context, attribute)
            }
        }
    }

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

