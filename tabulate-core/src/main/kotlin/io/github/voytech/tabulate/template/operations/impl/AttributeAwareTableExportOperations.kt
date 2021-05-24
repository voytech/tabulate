package io.github.voytech.tabulate.template.operations.impl

import io.github.voytech.tabulate.api.builder.TableBuilder
import io.github.voytech.tabulate.model.Table
import io.github.voytech.tabulate.model.attributes.CellAttribute
import io.github.voytech.tabulate.model.attributes.ColumnAttribute
import io.github.voytech.tabulate.model.attributes.RowAttribute
import io.github.voytech.tabulate.model.attributes.TableAttribute
import io.github.voytech.tabulate.template.context.AttributedCell
import io.github.voytech.tabulate.template.context.AttributedColumn
import io.github.voytech.tabulate.template.context.AttributedRow
import io.github.voytech.tabulate.template.context.narrow
import io.github.voytech.tabulate.template.operations.TableExportOperations


@Suppress("UNCHECKED_CAST")
class AttributeAwareTableExportOperations<T,O>(
    private val attributeOperations: AttributesOperations<T>,
    private val baseTableExportOperations: TableExportOperations<T,O>
) : TableExportOperations<T,O> by baseTableExportOperations {

    private fun sortedTableAttributes(tableAttributes: Set<TableAttribute<*>>?): Set<TableAttribute<*>>? {
        return tableAttributes?.toSortedSet(compareBy {
            attributeOperations.getTableAttributeOperation(it.javaClass)?.priority() ?: 0
        })
    }

    private fun sortedColumnAttributes(columnAttributes: Set<ColumnAttribute<*>>?): Set<ColumnAttribute<*>>? {
        return columnAttributes?.toSortedSet(compareBy {
            attributeOperations.getColumnAttributeOperation(it.javaClass)?.priority() ?: 0
        })
    }

    private fun sortedCellAttributes(cellAttributes: Set<CellAttribute<*>>?): Set<CellAttribute<*>>? {
        return cellAttributes?.sortedBy { attributeOperations.getCellAttributeOperation(it.javaClass)?.priority() ?: 0 }?.toSet()
    }

    private fun sortedRowAttributes(rowAttributes: Set<RowAttribute<*>>?): Set<RowAttribute<*>>? {
        return rowAttributes?.toSortedSet(compareBy { attributeOperations.getRowAttributeOperation(it.javaClass)?.priority() ?: 0 })
    }

    private fun withAllAttributesOperationSorted(builder: TableBuilder<T>): TableBuilder<T> {
        builder.columnsBuilder.columnBuilders.forEach { columnBuilder ->
            columnBuilder.visit(CellAttribute::class.java) { sortedCellAttributes(it) }
            columnBuilder.visit(ColumnAttribute::class.java) { sortedColumnAttributes(it) }
        }
        builder.rowsBuilder.rowBuilders.forEach { rowBuilder ->
            rowBuilder.visit(CellAttribute::class.java) { sortedCellAttributes(it) }
            rowBuilder.visit(RowAttribute::class.java) { sortedRowAttributes(it) }
            rowBuilder.cellsBuilder.cells.forEach { (_, cellBuilder) ->
                cellBuilder.visit(CellAttribute::class.java) { sortedCellAttributes(it) }
            }
        }
        builder.visit(TableAttribute::class.java) { sortedTableAttributes(it) }
        builder.visit(CellAttribute::class.java) { sortedCellAttributes(it) }
        return builder
    }

    override fun createTable(builder: TableBuilder<T>): Table<T> {
        return baseTableExportOperations.createTable(withAllAttributesOperationSorted(builder)).also { sortedTable ->
            sortedTable.tableAttributes?.forEach { tableAttribute ->
                attributeOperations.getTableAttributeOperation(tableAttribute.javaClass)?.renderAttribute(sortedTable, tableAttribute)
            }
        }
    }

    override fun beginRow(context: AttributedRow<T>) {
        if (!context.rowAttributes.isNullOrEmpty()) {
            var operationRendered = false
            context.rowAttributes.forEach { attribute ->
                attributeOperations.getRowAttributeOperation(attribute.javaClass)?.let { operation ->
                    if (operation.priority() >= 0 && !operationRendered) {
                        baseTableExportOperations.beginRow(context)
                        operationRendered = true
                    }
                    operation.renderAttribute(context.narrow(), attribute)
                }
            }
        } else {
            baseTableExportOperations.beginRow(context)
        }
    }

    override fun renderColumn(context: AttributedColumn) {
        context.columnAttributes?.let { attributes ->
            attributes.forEach { attribute ->
                attributeOperations.getColumnAttributeOperation(attribute.javaClass)
                    ?.renderAttribute(context.narrow(), attribute)
            }
        }
    }

    override fun renderRowCell(context: AttributedCell) {
        if (!context.attributes.isNullOrEmpty()) {
            var operationRendered = false
            context.attributes.forEach { attribute ->
                attributeOperations.getCellAttributeOperation(attribute.javaClass)?.let { operation ->
                    if (operation.priority() >= 0 && !operationRendered) {
                        baseTableExportOperations.renderRowCell(context)
                        operationRendered = true
                    }
                    operation.renderAttribute(context.narrow(), attribute)
                }
            }
        } else {
            baseTableExportOperations.renderRowCell(context)
        }
    }

}

