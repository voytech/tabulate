package io.github.voytech.tabulate.template.operations

import io.github.voytech.tabulate.api.builder.AttributeSetTransformer
import io.github.voytech.tabulate.api.builder.AttributeTransformerContainer
import io.github.voytech.tabulate.model.attributes.CellAttribute
import io.github.voytech.tabulate.model.attributes.ColumnAttribute
import io.github.voytech.tabulate.model.attributes.RowAttribute
import io.github.voytech.tabulate.model.attributes.TableAttribute
import io.github.voytech.tabulate.template.context.*

@Suppress("UNCHECKED_CAST")
internal class AttributeAwareTableExportOperations<T>(
    private val attributeOperations: AttributesOperations<T>,
    private val baseTableExportOperations: BasicContextExportOperations<T>,
    private val enableAttributeSetCaching: Boolean = true
) : TableExportOperations<T>  {

    inner class SortedTableAttributeSetTransformer: AttributeSetTransformer<TableAttribute<*>> {
        override fun transform(input: Set<TableAttribute<*>>): Set<TableAttribute<*>> =
            input.toSortedSet(compareBy {
                attributeOperations.getTableAttributeOperation(it.javaClass)?.priority() ?: 0
            })
    }

    inner class SortedColumnAttributeSetTransformer: AttributeSetTransformer<ColumnAttribute<*>> {
        override fun transform(input: Set<ColumnAttribute<*>>): Set<ColumnAttribute<*>> =
            input.toSortedSet(compareBy {
                attributeOperations.getColumnAttributeOperation(it.javaClass)?.priority() ?: 0
            })
    }

    inner class SortedRowAttributeSetTransformer: AttributeSetTransformer<RowAttribute<*>> {
        override fun transform(input: Set<RowAttribute<*>>): Set<RowAttribute<*>> =
            input.toSortedSet(compareBy {
                attributeOperations.getRowAttributeOperation(it.javaClass)?.priority() ?: 0
            })
    }
    inner class SortedCellAttributeSetTransformer: AttributeSetTransformer<CellAttribute<*>> {
        override fun transform(input: Set<CellAttribute<*>>): Set<CellAttribute<*>> =
            input.sortedBy {
                attributeOperations.getCellAttributeOperation(it.javaClass)?.priority() ?: 0
            }.toSet()
    }

    internal fun createAttributeTransformerContainer() : AttributeTransformerContainer {
        return AttributeTransformerContainer().also {
            it.set(TableAttribute::class.java, listOf(SortedTableAttributeSetTransformer()))
            it.set(ColumnAttribute::class.java, listOf(SortedColumnAttributeSetTransformer()))
            it.set(RowAttribute::class.java, listOf(SortedRowAttributeSetTransformer()))
            it.set(CellAttribute::class.java, listOf(SortedCellAttributeSetTransformer()))
        }
    }

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
            var operationRendered = false
            if (!context.rowAttributes.isNullOrEmpty()) {
                context.rowAttributes?.forEach { attribute ->
                    attributeOperations.getRowAttributeOperation(attribute.javaClass)?.let { operation ->
                        if (operation.priority() >= 0 && !operationRendered) {
                            baseTableExportOperations.beginRow(this)
                            operationRendered = true
                        }
                        operation.renderAttribute(this, attribute)
                    }
                }
            }
            if (!operationRendered) {
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
            var operationRendered = false
            if (!context.attributes.isNullOrEmpty()) {
                context.attributes.forEach { attribute ->
                    attributeOperations.getCellAttributeOperation(attribute.javaClass)?.let { operation ->
                        if (operation.priority() >= 0 && !operationRendered) {
                            baseTableExportOperations.renderRowCell(this)
                            operationRendered = true
                        }
                        operation.renderAttribute(this, attribute)
                    }
                }
            }
            if (!operationRendered){
                baseTableExportOperations.renderRowCell(this)
            }
        }
    }

    override fun endRow(context: AttributedRowWithCells<T>) {
        baseTableExportOperations.endRow(context.crop())
    }

}

