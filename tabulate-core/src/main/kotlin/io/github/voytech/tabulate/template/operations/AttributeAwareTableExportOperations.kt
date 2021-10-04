package io.github.voytech.tabulate.template.operations

import io.github.voytech.tabulate.api.builder.TableBuilder
import io.github.voytech.tabulate.api.builder.TableBuilderTransformer
import io.github.voytech.tabulate.model.attributes.CellAttribute
import io.github.voytech.tabulate.model.attributes.ColumnAttribute
import io.github.voytech.tabulate.model.attributes.RowAttribute
import io.github.voytech.tabulate.model.attributes.TableAttribute
import io.github.voytech.tabulate.template.context.*

interface BasicContextExportOperations<T> {
    fun createTable(context: TableContext) {}
    fun renderColumn(context: ColumnContext) {}
    fun beginRow(context: RowContext<T>) {}
    fun renderRowCell(context: RowCellContext)
    fun endRow(context: RowContextWithCells<T>) {}
}

@Suppress("UNCHECKED_CAST")
class AttributeAwareTableExportOperations<T>(
    private val attributeOperations: AttributesOperations<T>,
    private val baseTableExportOperations: BasicContextExportOperations<T>,
    private val enableAttributeSetCaching: Boolean = true
) : TableBuilderTransformer<T>, TableExportOperations<T>  {

    private fun sortedTableAttributes(tableAttributes: Set<TableAttribute<*>>): Set<TableAttribute<*>> {
        return tableAttributes.toSortedSet(compareBy {
            attributeOperations.getTableAttributeOperation(it.javaClass)?.priority() ?: 0
        })
    }

    private fun sortedColumnAttributes(columnAttributes: Set<ColumnAttribute<*>>): Set<ColumnAttribute<*>> {
        return columnAttributes.toSortedSet(compareBy {
            attributeOperations.getColumnAttributeOperation(it.javaClass)?.priority() ?: 0
        })
    }

    private fun sortedCellAttributes(cellAttributes: Set<CellAttribute<*>>): Set<CellAttribute<*>> {
        return cellAttributes.sortedBy { attributeOperations.getCellAttributeOperation(it.javaClass)?.priority() ?: 0 }
            .toSet()
    }

    private fun sortedRowAttributes(rowAttributes: Set<RowAttribute<*>>): Set<RowAttribute<*>> {
        return rowAttributes.toSortedSet(compareBy { attributeOperations.getRowAttributeOperation(it.javaClass)?.priority() ?: 0 })
    }

    private fun withAllAttributesOperationSorted(builder: TableBuilder<T>): TableBuilder<T> {
        builder.columnsBuilder.columnBuilders.forEach { columnBuilder ->
            columnBuilder.visit(CellAttribute::class.java) { sortedCellAttributes(it) }
            columnBuilder.visit(ColumnAttribute::class.java) { sortedColumnAttributes(it) }
        }
        builder.rowsBuilder.rowBuilders.forEach { rowBuilder ->
            rowBuilder.visit(CellAttribute::class.java) { sortedCellAttributes(it) }
            rowBuilder.visit(RowAttribute::class.java) { sortedRowAttributes(it) }
            rowBuilder.cells.forEach { (_, cellBuilder) ->
                cellBuilder.visit(CellAttribute::class.java) { sortedCellAttributes(it) }
            }
        }
        builder.visit(TableAttribute::class.java) { sortedTableAttributes(it) }
        builder.visit(CellAttribute::class.java) { sortedCellAttributes(it) }
        return builder
    }

    override fun transform(builder: TableBuilder<T>): TableBuilder<T> = withAllAttributesOperationSorted(builder)

    override fun createTable(context: AttributedTable) {
        with(context.crop()) {
            return baseTableExportOperations.createTable(this).also {
                context.tableAttributes?.forEach { tableAttribute ->
                    attributeOperations.getTableAttributeOperation(tableAttribute.javaClass)?.renderAttribute(this, tableAttribute)
                }
            }
        }
    }

    override fun beginRow(context: AttributedRow<T>) {
        with(context.crop()) {
            if (!context.rowAttributes.isNullOrEmpty()) {
                var operationRendered = false
                context.rowAttributes?.forEach { attribute ->
                    attributeOperations.getRowAttributeOperation(attribute.javaClass)?.let { operation ->
                        if (operation.priority() >= 0 && !operationRendered) {
                            baseTableExportOperations.beginRow(this)
                            operationRendered = true
                        }
                        operation.renderAttribute(this, attribute)
                    }
                }
            } else {
                baseTableExportOperations.beginRow(this)
            }
        }
    }

    override fun renderColumn(context: AttributedColumn) {
        with(context.crop()) {
            context.columnAttributes?.let { attributes ->
                attributes.forEach { attribute ->
                    attributeOperations.getColumnAttributeOperation(attribute.javaClass)
                        ?.renderAttribute(this, attribute)
                }
            }
        }
    }

    override fun renderRowCell(context: AttributedCell) {
        if (enableAttributeSetCaching) context.ensureAttributesCacheEntry()
        with(context.crop()) {
            if (!context.attributes.isNullOrEmpty()) {
                var operationRendered = false
                context.attributes.forEach { attribute ->
                    attributeOperations.getCellAttributeOperation(attribute.javaClass)?.let { operation ->
                        if (operation.priority() >= 0 && !operationRendered) {
                            baseTableExportOperations.renderRowCell(this)
                            operationRendered = true
                        }
                        operation.renderAttribute(this, attribute)
                    }
                }
            } else {
                baseTableExportOperations.renderRowCell(this)
            }
        }
    }

    override fun endRow(context: AttributedRowWithCells<T>) {
        baseTableExportOperations.endRow(context.crop())
    }

}

