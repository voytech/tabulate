package io.github.voytech.tabulate.core.template.operations.impl

import io.github.voytech.tabulate.core.api.builder.TableBuilder
import io.github.voytech.tabulate.core.model.Table
import io.github.voytech.tabulate.core.model.attributes.alias.CellAttribute
import io.github.voytech.tabulate.core.model.attributes.alias.ColumnAttribute
import io.github.voytech.tabulate.core.model.attributes.alias.RowAttribute
import io.github.voytech.tabulate.core.model.attributes.alias.TableAttribute
import io.github.voytech.tabulate.core.template.operations.TableOperation
import io.github.voytech.tabulate.core.model.attributes.CellAttribute as CellAttributeClass
import io.github.voytech.tabulate.core.model.attributes.ColumnAttribute as ColumnAttributeClass
import io.github.voytech.tabulate.core.model.attributes.RowAttribute as RowAttributeClass
import io.github.voytech.tabulate.core.model.attributes.TableAttribute as TableAttributeClass

@Suppress("UNCHECKED_CAST")
class AttributeAwareTableOperations<T>(
    private val attributeOperations: AttributesOperations<T>,
    private val baseLifecycleOperations: TableOperation<T>
) : TableOperation<T> {

    private fun sortedTableAttributes(tableAttributes: Set<TableAttribute>?): Set<TableAttribute>? {
        return tableAttributes?.toSortedSet(compareBy {
            attributeOperations.getTableAttributeOperation(it.javaClass)?.priority() ?: 0
        })
    }

    private fun sortedColumnAttributes(columnAttributes: Set<ColumnAttribute>?): Set<ColumnAttribute>? {
        return columnAttributes?.toSortedSet(compareBy {
            attributeOperations.getColumnAttributeOperation(it.javaClass)?.priority() ?: 0
        })
    }

    private fun sortedCellAttributes(cellAttributes: Set<CellAttribute>?): Set<CellAttribute>? {
        return cellAttributes?.sortedBy { attributeOperations.getCellAttributeOperation(it.javaClass)?.priority() ?: 0 }?.toSet()
    }

    private fun sortedRowAttributes(rowAttributes: Set<RowAttribute>?): Set<RowAttribute>? {
        return rowAttributes?.toSortedSet(compareBy { attributeOperations.getRowAttributeOperation(it.javaClass)?.priority() ?: 0 })
    }

    private fun withAllAttributesOperationSorted(builder: TableBuilder<T>): TableBuilder<T> {
        builder.columnsBuilder.columnBuilders.forEach { columnBuilder ->
            columnBuilder.visit(CellAttributeClass::class.java) { sortedCellAttributes(it) }
            columnBuilder.visit(ColumnAttributeClass::class.java) { sortedColumnAttributes(it) }
        }
        builder.rowsBuilder.rowBuilders.forEach { rowBuilder ->
            rowBuilder.visit(CellAttributeClass::class.java) { sortedCellAttributes(it) }
            rowBuilder.visit(RowAttributeClass::class.java) { sortedRowAttributes(it) }
            rowBuilder.cellsBuilder.cells.forEach { (_, cellBuilder) ->
                cellBuilder.visit(CellAttributeClass::class.java) { sortedCellAttributes(it) }
            }
        }
        builder.visit(TableAttributeClass::class.java) { sortedTableAttributes(it) }
        builder.visit(CellAttributeClass::class.java) { sortedCellAttributes(it) }
        return builder
    }

    override fun createTable(builder: TableBuilder<T>): Table<T> {
        return baseLifecycleOperations.createTable(withAllAttributesOperationSorted(builder)).also { sortedTable ->
            sortedTable.tableAttributes?.forEach { tableAttribute ->
                attributeOperations.getTableAttributeOperation(tableAttribute.javaClass)?.renderAttribute(sortedTable, tableAttribute)
            }
        }
    }

}

